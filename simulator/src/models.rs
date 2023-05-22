use crate::config::Config;
use derive_more::Display;
use serde::{Deserialize, Serialize};

/// The different tiers of achievement diaries
#[derive(Copy, Clone, Debug, Display, PartialEq, PartialOrd, Serialize, Deserialize)]
pub enum AchievementDiaryLevel {
    Easy,
    Medium,
    Hard,
    Elite,
}

/// Different types of compost that can be applied to a farming patch
#[derive(Copy, Clone, Debug, Default, Display, PartialEq, Serialize, Deserialize)]
pub enum Compost {
    #[default]
    None,
    Normal,
    Supercompost,
    Ultracompost,
}

impl Compost {
    /// Get disease chance *as a fraction of 128*. Disease chance is always
    /// expressed as a *proper* fraction of 128, i.e. the numerator is always a
    /// whole number. So we'll calculate the numerator separately, then divide
    /// after applying modifiers.
    /// https://oldschool.runescape.wiki/w/Disease_(Farming)#Reducing_disease_risk
    pub fn disease_base_chance(self) -> f64 {
        match self {
            Self::None => 27.0,
            Self::Normal => 14.0,
            Self::Supercompost => 6.0,
            Self::Ultracompost => 3.0,
        }
    }

    /// TODO
    pub fn initial_lives(self) -> usize {
        match self {
            Self::None => 3,
            Self::Normal => 4,
            Self::Supercompost => 5,
            Self::Ultracompost => 6,
        }
    }
}

/// A type of plant that has global impact on how other crops grow
/// https://oldschool.runescape.wiki/w/Anima_seed
#[derive(Copy, Clone, Debug, Display, PartialEq, Serialize, Deserialize)]
pub enum AnimaPlant {
    /// https://oldschool.runescape.wiki/w/Kronos_seed
    Kronos,
    /// Increases yield https://oldschool.runescape.wiki/w/Attas_seed
    Attas,
    /// Lowers disease chance https://oldschool.runescape.wiki/w/Iasor_seed
    Iasor,
}

/// The different types of herbs that a player can grow in an herb patch
#[derive(Copy, Clone, Debug, Display)]
pub enum Herb {
    #[display(fmt = "Guam leaf")]
    Guam,
    Marrentill,
    Tarromin,
    Harralander,
    Goutweed,
    #[display(fmt = "Ranarr weed")]
    Ranarr,
    Toadflax,
    #[display(fmt = "Irit leaf")]
    Irit,
    Avantoe,
    Kwuarm,
    Snapdragon,
    Cadantine,
    Lantadyme,
    #[display(fmt = "Dwarf weed")]
    Dwarf,
    Torstol,
}

impl Herb {
    pub const ALL: &[Herb] = &[
        Self::Guam,
        Self::Marrentill,
        Self::Tarromin,
        Self::Harralander,
        Self::Goutweed,
        Self::Ranarr,
        Self::Toadflax,
        Self::Irit,
        Self::Avantoe,
        Self::Kwuarm,
        Self::Snapdragon,
        Self::Cadantine,
        Self::Lantadyme,
        Self::Dwarf,
        Self::Torstol,
    ];

    /// Get the farming level required to plant this herb
    pub fn farming_level(self) -> usize {
        match self {
            Self::Guam => 9,
            Self::Marrentill => 14,
            Self::Tarromin => 19,
            Self::Harralander => 26,
            Self::Goutweed => 29,
            Self::Ranarr => 32,
            Self::Toadflax => 38,
            Self::Irit => 44,
            Self::Avantoe => 50,
            Self::Kwuarm => 56,
            Self::Snapdragon => 62,
            Self::Cadantine => 67,
            Self::Lantadyme => 73,
            Self::Dwarf => 79,
            Self::Torstol => 85,
        }
    }

    /// Get the "chance to save" for an herb at level 1 and level 99. All other
    /// level's values can be linearly interpolated from there.
    ///
    /// See https://oldschool.runescape.wiki/w/Farming#Variable_crop_yield
    pub fn chance_to_save(self) -> (f64, f64) {
        // Values are ripped from https://oldschool.runescape.wiki/w/Calculator:Farming/Herbs/Template
        match self {
            Self::Guam => (25.0, 80.0),
            Self::Marrentill => (28.0, 80.0),
            Self::Tarromin => (31.0, 80.0),
            Self::Harralander => (36.0, 80.0),
            Self::Goutweed => (39.0, 80.0),
            Self::Ranarr => (39.0, 80.0),
            Self::Toadflax => (43.0, 80.0),
            Self::Irit => (46.0, 80.0),
            Self::Avantoe => (50.0, 80.0),
            Self::Kwuarm => (54.0, 80.0),
            Self::Snapdragon => (57.0, 80.0),
            Self::Cadantine => (60.0, 80.0),
            Self::Lantadyme => (64.0, 80.0),
            Self::Dwarf => (67.0, 80.0),
            Self::Torstol => (71.0, 80.0),
        }
    }

    /// The amount of Farming XP gained for *planting* one seed of this herb
    pub fn xp_per_plant(self) -> f64 {
        match self {
            Self::Guam => 11.0,
            Self::Marrentill => 13.5,
            Self::Tarromin => 16.0,
            Self::Harralander => 21.5,
            Self::Goutweed => 105.0,
            Self::Ranarr => 27.0,
            Self::Toadflax => 34.0,
            Self::Irit => 43.0,
            Self::Avantoe => 54.5,
            Self::Kwuarm => 69.0,
            Self::Snapdragon => 87.5,
            Self::Cadantine => 106.5,
            Self::Lantadyme => 134.5,
            Self::Dwarf => 170.5,
            Self::Torstol => 199.5,
        }
    }

    /// The amount of Farming XP gained for *harvesting* one herb
    pub fn xp_per_harvest(self) -> f64 {
        match self {
            Self::Guam => 12.5,
            Self::Marrentill => 15.0,
            Self::Tarromin => 18.0,
            Self::Harralander => 24.0,
            Self::Goutweed => 45.0,
            Self::Ranarr => 30.5,
            Self::Toadflax => 38.5,
            Self::Irit => 48.5,
            Self::Avantoe => 61.5,
            Self::Kwuarm => 78.0,
            Self::Snapdragon => 98.5,
            Self::Cadantine => 120.0,
            Self::Lantadyme => 151.5,
            Self::Dwarf => 192.0,
            Self::Torstol => 224.5,
        }
    }
}

/// An herb farming patch.
#[derive(Copy, Clone, Debug, Display, PartialEq, Serialize, Deserialize)]
pub enum HerbPatch {
    Ardougne,
    Catherby,
    Falador,
    FarmingGuild,
    HarmonyIsland,
    Hosidius,
    PortPhasmatys,
    TrollStronghold,
    Weiss,
}

impl HerbPatch {
    /// Get a descriptive string that includes this patch's name and all of its
    /// buffs
    pub fn description(self, config: &Config) -> String {
        // Start with the patch name
        let mut description = self.to_string();

        let disease_free = self.disease_free(config);
        let chance_to_save_bonus = self.chance_to_save_bonus(config);
        let xp_bonus = self.xp_bonus(config);

        // Apply modifiers
        let mut modifiers = Vec::new();
        if disease_free {
            modifiers.push("disease-free".to_owned());
        }
        if chance_to_save_bonus > 0.0 {
            modifiers.push(format!("{:+}% yield", chance_to_save_bonus * 100.0));
        }
        if xp_bonus > 0.0 {
            modifiers.push(format!("{:+}% XP", xp_bonus * 100.0));
        }

        if !modifiers.is_empty() {
            description.push_str(&format!(" ({})", modifiers.join(", ")));
        }

        description
    }

    /// Is this patch 100% certified disease-free? This can depend on patch
    /// modifiers so we need the config available.
    pub fn disease_free(self, config: &Config) -> bool {
        match self {
            Self::TrollStronghold | Self::Weiss => true,
            Self::Hosidius => config.hosidius_fifty_favor,
            _ => false,
        }
    }

    /// Calculate the "chance to save a life" that this patch provides. This
    /// will stack with other bonuses (magic secateurs, etc.). This can depend
    /// on patch modifiers so we need the config available.
    ///
    /// See https://oldschool.runescape.wiki/w/Farming#Variable_crop_yield
    pub fn chance_to_save_bonus(self, config: &Config) -> f64 {
        match (self, config) {
            // Bonus scales based on tiers completed
            (
                Self::Catherby,
                Config {
                    kandarin_diary: Some(diary),
                    ..
                },
            ) => match diary {
                AchievementDiaryLevel::Easy => 0.0,
                AchievementDiaryLevel::Medium => 0.05,
                AchievementDiaryLevel::Hard => 0.10,
                AchievementDiaryLevel::Elite => 0.15,
            },

            // Both get +5% from Kourend medium
            (
                Self::FarmingGuild | Self::Hosidius,
                Config {
                    kourend_diary: Some(diary),
                    ..
                },
            ) if *diary >= AchievementDiaryLevel::Hard => 0.05,

            _ => 0.0,
        }
    }

    /// Get the XP bonus that this patch provides for **all actions** performed
    /// on the patch. This can depend on patch modifiers so we need the config
    /// available.
    pub fn xp_bonus(self, config: &Config) -> f64 {
        match (self, config) {
            // Falador Medium grants a +10% XP bonus
            (
                Self::Falador,
                Config {
                    falador_diary: Some(diary),
                    ..
                },
            ) if *diary >= AchievementDiaryLevel::Medium => 0.10,
            _ => 0.0,
        }
    }

    /// The odds that an herb growing in this patch survives during a single
    /// cycle transition
    pub fn calc_disease_chance_per_cycle(self, config: &Config) -> f64 {
        // https://oldschool.runescape.wiki/w/Seeds#Herb_seeds
        // https://oldschool.runescape.wiki/w/Disease_(Farming)#Reducing_disease_risk
        if self.disease_free(config) {
            0.0
        } else {
            // These probs are all in multiples of 1/128, so we'll just work
            // with numerators then do the division at the end
            // Base chance is based on compost
            let base_chance = config.compost.disease_base_chance();

            // Iasor reduces chance by 80%
            let modifier = match config.anima_plant {
                Some(AnimaPlant::Iasor) => 0.2,
                _ => 1.0,
            };

            // Round *down* to the nearest multiple of 1/128, but not to zero
            let numerator = f64::max(f64::floor(base_chance * modifier), 1.0);
            numerator / 128.0
        }
    }

    /// Calculate the chance to "save a life" when picking an herb. This is
    /// variable based on the herb, farming level, and applicable yield bonuses.
    ///
    /// See https://oldschool.runescape.wiki/w/Farming#Variable_crop_yield
    pub fn calc_chance_to_save(self, config: &Config, herb: Herb) -> f64 {
        let item_bonus = config.calc_item_chance_to_save();
        let diary_bonus = self.chance_to_save_bonus(config);
        let attas_bonus = match config.anima_plant {
            Some(AnimaPlant::Attas) => 0.05,
            _ => 0.0,
        };

        let (chance1, chance99) = herb.chance_to_save();

        // This comes straight from the wiki, it's a lot easier to read in
        // their formatting (link above). The formatted formula doesn't mention
        // anything about the `floor`s though, but it's in the calculator source
        // https://oldschool.runescape.wiki/w/Calculator:Template/Farming/Herbs2?action=edit
        f64::floor(
            f64::floor(f64::floor((chance1 * (99.0 - config.farming_level as f64) / 98.0)
            + (chance99 * (config.farming_level as f64 - 1.0) / 98.0))
                * (1.0 + item_bonus))
                // Attas doesn't appear in the formula on the page above, but
                // it's also in the calculator source (see link above)
                * (1.0 + diary_bonus + attas_bonus)
                + 1.0,
        ) / 256.0
    }
}
