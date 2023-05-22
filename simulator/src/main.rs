mod config;
mod models;
mod state;
mod util;

use crate::{
    config::Config,
    models::{Herb, HerbPatch},
    state::HerbPatchState,
};
use clap::Parser;
use comfy_table::{presets, Row, Table};
use derive_more::{Add, Sum};
use indicatif::ProgressBar;
use std::path::PathBuf;

/// Herb farming simulator
#[derive(Debug, Parser)]
struct Args {
    #[arg(long, short, default_value_t = 10000)]
    trials: usize,

    config_path: PathBuf,
}

fn main() {
    let args = Args::parse();
    let config = Config::load(&args.config_path);

    println!("Running {} trials per herb...", args.trials);
    println!();

    // Run n simulations for each herb+patch
    for &herb in Herb::ALL {
        println!("{}", herb);
        if herb.farming_level() <= config.farming_level {
            let pb = ProgressBar::new((args.trials * config.patches.len()) as u64);
            let mut table = Table::new();
            table
                .load_preset(presets::ASCII_BORDERS_ONLY_CONDENSED)
                .set_header(["Patch", "Harvested", "XP Gained", "Survive%"]);

            // Run n simulations for each patch
            let mut all_results = Vec::new();
            for &patch in &config.patches {
                let mut result = AggregateResult::default();
                for _ in 0..args.trials {
                    simulate_patch(&config, herb, patch, &mut result);
                    pb.inc(1);
                }
                table.add_row(result.format_row(patch.to_string()));
                all_results.push(result);
            }

            // Print results
            pb.finish_and_clear();
            let sum: AggregateResult = all_results.into_iter().sum();
            table.add_row(sum.format_row("All".into()));
            println!("{}", table);
        } else {
            println!("Requires level {}", herb.farming_level());
        }
        println!();
    }
}

fn simulate_patch(config: &Config, herb: Herb, patch: HerbPatch, result: &mut AggregateResult) {
    let mut state = HerbPatchState::new(config, patch, herb);
    if state.grow(config) {
        state.harvest(config);
    }

    result.add_trial(state);
}

#[derive(Add, Copy, Clone, Debug, Default, Sum)]
struct AggregateResult {
    num_trials: usize,
    num_patches: usize,
    herbs_harvested: usize,
    xp_gained: f64,
    num_survived: usize,
    num_resurrected: usize,
}

impl AggregateResult {
    fn format_row(&self, patch_name: String) -> impl Into<Row> {
        let trials = self.num_trials as f64;
        let patches = self.num_patches as f64;
        [
            patch_name,
            // Total herbs per run
            format!("{:.2}", self.herbs_harvested as f64 / trials * patches),
            format!("{:.0}", self.xp_gained / trials * patches),
            // Survival rate *shouldn't* multiply by patches
            format!("{:.1}", self.num_survived as f64 / trials * 100.0),
        ]
    }

    fn add_trial(&mut self, state: HerbPatchState) {
        if self.num_patches == 0 {
            self.num_patches = 1;
        }

        self.num_trials += 1;
        self.herbs_harvested += state.herbs_harvested();
        self.xp_gained += state.xp_gained();
        if state.survived() {
            self.num_survived += 1;
        }
        if state.resurrected() {
            self.num_resurrected += 1;
        }
    }
}
