import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.resolve(__dirname, '..');
const read = (file) => fs.readFileSync(path.join(root, file), 'utf8');
const assert = (condition, message) => {
  if (!condition) throw new Error(message);
};

const modBlocks = read('src/main/java/io/github/n013ody/createaddon/registry/ModBlocks.java');
const creativeTabs = read('src/main/java/io/github/n013ody/createaddon/registry/ModCreativeTabs.java');
const client = read('src/main/java/io/github/n013ody/createaddon/client/CreateAddonClient.java');
const ponderScenes = read('src/main/java/io/github/n013ody/createaddon/client/ponder/GuidancePonderScenes.java');
const enUs = JSON.parse(read('src/main/resources/assets/createaddon/lang/en_us.json'));
const zhCn = JSON.parse(read('src/main/resources/assets/createaddon/lang/zh_cn.json'));

const registry = new Map();
const blockConstantsById = new Map();

for (const match of modBlocks.matchAll(/DeferredBlock<.+>\s+([A-Z0-9_]+)\s*=\s*(?:register\w+Block|BLOCKS\.register)\(\s*"([^"]+)"/g)) {
  registry.set(match[1], { type: 'block', id: match[2] });
  blockConstantsById.set(match[2], match[1]);
}

for (const match of modBlocks.matchAll(/DeferredItem<([^>]+)>\s+([A-Z0-9_]+)\s*=\s*ITEMS\.register\("([^"]+)"/g)) {
  registry.set(match[2], {
    type: match[1].includes('BlockItem') ? 'block' : 'item',
    id: match[3],
  });
}

for (const match of modBlocks.matchAll(/DeferredItem<BlockItem>\s+([A-Z0-9_]+)\s*=\s*register(?:Machine|Sensor)Item\("([^"]+)"/g)) {
  registry.set(match[1], { type: 'block', id: match[2] });
}

for (const match of modBlocks.matchAll(/DeferredItem<BlockItem>\s+([A-Z0-9_]+)\s*=\s*registerComputingItem\("([^"]+)"/g)) {
  registry.set(match[1], { type: 'block', id: match[2] });
}

const acceptedConstants = [...creativeTabs.matchAll(/output\.accept\(ModBlocks\.([A-Z0-9_]+)\.get\(\)\)/g)]
  .map((match) => match[1]);

assert(acceptedConstants.length > 0, 'No creative tab entries found to validate');

for (const constantName of acceptedConstants) {
  const entry = registry.get(constantName);
  assert(entry, `No registry entry found for creative tab constant ${constantName}`);

  assert(
    client.includes(`registerTooltip(ModBlocks.${constantName}.get())`)
      || client.includes(`registerBlockTooltip(ModBlocks.${constantName}.get()`),
    `Create tooltip not registered for ${constantName}`,
  );

  const keyPrefix = `${entry.type}.createaddon.${entry.id}.tooltip`;
  assert(enUs[`${keyPrefix}.summary`], `Missing English tooltip summary for ${entry.id}`);
  assert(zhCn[`${keyPrefix}.summary`], `Missing Chinese tooltip summary for ${entry.id}`);

  if (entry.type === 'block') {
    const blockConstant = blockConstantsById.get(entry.id);
    assert(blockConstant, `No block constant found for ${entry.id}`);
    assert(
      ponderScenes.includes(`forComponents(ModBlocks.${blockConstant}.get())`),
      `Ponder scene not registered for block ${entry.id}`,
    );
  }
}

console.log('Create tooltip coverage validated');
