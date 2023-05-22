use crate::{
    config::Config,
    models::{Herb, HerbPatch},
    util::rand_bool,
};

/// Growth stages vary 0-4, with 4 being fully grown
const NUM_GROWTH_STAGES: usize = 5;
const FINAL_GROWTH_STAGE: usize = NUM_GROWTH_STAGES - 1;

#[derive(Copy, Clone, Debug)]
pub struct HerbPatchState {
    // Config
    patch: HerbPatch,
    herb: Herb,
    // Current state
    growth_stage: usize,
    status: Status,
    has_resurrected: bool,
    lives: usize,
    // Results
    herbs_harvested: usize,
    xp_gained: f64,
}

impl HerbPatchState {
    pub fn new(config: &Config, patch: HerbPatch, herb: Herb) -> Self {
        Self {
            patch,
            herb,
            growth_stage: 0,
            status: Status::Healthy,
            has_resurrected: false,
            lives: config.compost.initial_lives(),
            herbs_harvested: 0,
            xp_gained: 0.0,
        }
    }

    /// TODO
    pub fn grow(&mut self, config: &Config) -> bool {
        let disease_chance_per_cycle = self.patch.calc_disease_chance_per_cycle(config);
        let resurrect_chance = 0.0; // TODO

        // Grow until we hit a terminal stage
        while self.growth_stage < FINAL_GROWTH_STAGE && self.status != Status::Dead {
            // It's not clear to me why the wiki uses n-1 as the exponent in
            // the overall death formula, since disease can occur at any growth
            // stage (i.e. all 4), and stops growth meaning death should also be
            // able to occur at any growth stage. Needs more investigation.
            // https://oldschool.runescape.wiki/w/Disease_(Farming)

            if self.status == Status::Healthy && rand_bool(disease_chance_per_cycle) {
                // When disease is applied, the plant stops growing
                self.status = Status::Diseased;
            } else if self.status == Status::Diseased {
                self.status = Status::Dead;
            }

            // If the plant is dead, attempt a resurrection. We can only attempt
            // a resurrection once per crop.
            if self.status == Status::Dead
                && config.resurrect_crops
                && !self.has_resurrected
                && rand_bool(resurrect_chance)
            {
                self.status = Status::Healthy;
                self.has_resurrected = true;
                // Do *not* progress after resurrection, we have to pass
                // disease check again before progressing
            } else if self.status == Status::Healthy {
                // If plant is still healthy, we get to move on
                self.growth_stage += 1;
            }
        }

        self.status == Status::Healthy
    }

    /// TODO
    pub fn harvest(&mut self, config: &Config) {
        if self.status != Status::Healthy || self.growth_stage < FINAL_GROWTH_STAGE {
            panic!(
                "Can't harvest with status {:?} and growth stage {}",
                self.status, self.growth_stage
            );
        }

        let chance_to_save = self.patch.calc_chance_to_save(config, self.herb);

        while self.lives > 0 {
            self.herbs_harvested += 1;
            // If we get unlucky, take away a life
            if !rand_bool(chance_to_save) {
                self.lives -= 1;
            }
        }
    }

    pub fn herbs_harvested(self) -> usize {
        self.herbs_harvested
    }

    pub fn xp_gained(self) -> f64 {
        self.xp_gained
    }

    pub fn survived(self) -> bool {
        self.status == Status::Healthy
    }

    pub fn resurrected(self) -> bool {
        self.has_resurrected
    }
}

#[derive(Copy, Clone, Debug, Eq, PartialEq)]
enum Status {
    Healthy,
    Diseased,
    Dead,
}
