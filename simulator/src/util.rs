/// Get a random bool with a given weight of being *true*.
pub fn rand_bool(weight: f64) -> bool {
    rand::random::<f64>() < weight
}
