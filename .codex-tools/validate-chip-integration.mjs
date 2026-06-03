import fs from 'node:fs';
import path from 'node:path';

const root = process.cwd();
const failures = [];

const chipGatedRecipes = [
  'guidance_computer',
  'relative_bearing_sensor',
  'closing_speed_sensor',
  'terrain_altimeter',
  'inertial_drift_sensor',
  'lock_on_probability_sensor',
  'structural_stress_sensor',
  'relative_velocity_vector_sensor',
  'laser_range_finder',
  'radar_indexer',
  'threshold_controller'
];

function readJson(rel) {
  const file = path.join(root, rel);
  if (!fs.existsSync(file)) {
    failures.push(`missing ${rel}`);
    return undefined;
  }
  try {
    return JSON.parse(fs.readFileSync(file, 'utf8'));
  } catch (error) {
    failures.push(`invalid JSON ${rel}: ${error.message}`);
    return undefined;
  }
}

for (const name of chipGatedRecipes) {
  const rel = `src/main/resources/data/createaddon/recipe/${name}.json`;
  const recipe = readJson(rel);
  const text = JSON.stringify(recipe ?? {});
  if (!text.includes('createaddon:basic_logic_chip')) {
    failures.push(`${name} recipe should consume createaddon:basic_logic_chip`);
  }
}

const guidance = readJson('src/main/resources/data/createaddon/recipe/guidance_computer.json');
if (JSON.stringify(guidance ?? {}).includes('create:precision_mechanism') && !JSON.stringify(guidance ?? {}).includes('createaddon:basic_logic_chip')) {
  failures.push('guidance_computer should move its logic core from only precision mechanism to basic logic chip');
}

if (failures.length) {
  console.error(`Chip integration validation failed (${failures.length} issues):`);
  for (const failure of failures) console.error(`- ${failure}`);
  process.exit(1);
}

console.log('Chip integration validation passed');
