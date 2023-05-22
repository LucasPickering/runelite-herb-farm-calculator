use crate::models::{AchievementDiaryLevel, AnimaPlant, Compost, HerbPatch};
use figment::{
    providers::{Format, Json, Serialized},
    Figment,
};
use serde::{Deserialize, Serialize};
use std::path::Path;

/// Configuration related to a player's herb patches
#[derive(Clone, Debug, Default, PartialEq, Serialize, Deserialize)]
pub struct Config {
    /// The list of herb patches being farmed
    pub patches: Vec<HerbPatch>,

    /// Player farming level
    pub farming_level: usize,
    /// Player magic level (for resurrection)
    pub magic_level: usize,
    /// Do you have magic secateurs equipped? (10% yield bonus)
    pub magic_secateurs: bool,
    /// Do you have a farming cape equipped? (5% yield bonus)
    pub farming_cape: bool,
    /// Do you have a bottomless bucket? Affects cost of compost per patch
    pub bottomless_bucket: bool,
    /// Do you use the Resurrect Crops spell on patches that die?
    pub resurrect_crops: bool,
    // Global-level modifiers, that apply to all patches
    /// The type of compost being used
    pub compost: Compost,
    /// The type of Anima plant currently alive at the Farming Guild (can
    /// affect disease and yield rates)
    pub anima_plant: Option<AnimaPlant>,

    // Patch-level modifiers, that apply only to individual patches (some
    // fields will affect more than one patch, but none apply to *all*
    // patches)
    /// What level of Falador diary has the user completed? Affects XP from the
    /// Falador patch:
    /// Medium => +10% XP
    pub falador_diary: Option<AchievementDiaryLevel>,
    /// Does the user have *at least* 50% favor with Hosidius house? Makes the
    /// Hosidius patch disease-free
    pub hosidius_fifty_favor: bool,
    /// What level of Kandarin diary has the user completed? Provides a yield
    /// buff on the Catherby patch via increased chance to save a harvest life:
    /// Medium => +5%
    /// Hard => +10%
    /// Elite => +15%
    pub kandarin_diary: Option<AchievementDiaryLevel>,
    /// What level of Kourend diary has the user completed? Even though the
    /// buff is based solely on the whether *medium* is completed, we ask
    /// for the exact level just for consistency with other fields. Affects
    /// yield of the Farming Guild and Hosidius patches:
    /// Hard => +5% chance to save a harvest life
    pub kourend_diary: Option<AchievementDiaryLevel>,
}

impl Config {
    /// Load config data from the pre-defined config file path. Any missing
    /// values will be populated with defaults.
    pub fn load(path: &Path) -> Self {
        Figment::from(Serialized::defaults(Self::default()))
            .merge(Json::file(&path))
            .extract()
            .unwrap()
    }

    /// Calculate the "chance to save" bonus based on **equipped items** only.
    ///
    /// See https://oldschool.runescape.wiki/w/Farming#Variable_crop_yield
    pub fn calc_item_chance_to_save(&self) -> f64 {
        let mut bonus = 0.0;
        if self.magic_secateurs {
            bonus += 0.1;
        }
        if self.farming_cape {
            bonus += 0.05;
        }
        bonus
    }
}
