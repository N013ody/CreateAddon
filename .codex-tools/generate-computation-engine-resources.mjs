import fs from 'node:fs';
import path from 'node:path';
import zlib from 'node:zlib';

const root = process.cwd();

const blocks = [
  'function_setting_drum',
  'difference_register_column',
  'table_printer',
  'punched_card_reader',
  'mill_arithmetic_block',
  'store_memory_column',
  'mechanical_printer',
];

const items = [
  'blank_table_paper',
  'basic_processing_curve_table',
  'blank_punched_card',
  'machine_calibration_card',
];

function writeJson(rel, data) {
  const file = path.join(root, rel);
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, `${JSON.stringify(data, null, 2)}\n`, 'utf8');
}

function crc32(buf) {
  let crc = 0xffffffff;
  for (const byte of buf) {
    crc ^= byte;
    for (let i = 0; i < 8; i++) {
      crc = (crc >>> 1) ^ (0xedb88320 & -(crc & 1));
    }
  }
  return (crc ^ 0xffffffff) >>> 0;
}

function chunk(type, data) {
  const typeBuf = Buffer.from(type, 'ascii');
  const out = Buffer.alloc(12 + data.length);
  out.writeUInt32BE(data.length, 0);
  typeBuf.copy(out, 4);
  data.copy(out, 8);
  out.writeUInt32BE(crc32(Buffer.concat([typeBuf, data])), 8 + data.length);
  return out;
}

function png(width, height, rgba) {
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(width, 0);
  ihdr.writeUInt32BE(height, 4);
  ihdr[8] = 8;
  ihdr[9] = 6;
  const raw = Buffer.alloc((width * 4 + 1) * height);
  for (let y = 0; y < height; y++) {
    raw[y * (width * 4 + 1)] = 0;
    rgba.copy(raw, y * (width * 4 + 1) + 1, y * width * 4, (y + 1) * width * 4);
  }
  return Buffer.concat([
    Buffer.from('89504e470d0a1a0a', 'hex'),
    chunk('IHDR', ihdr),
    chunk('IDAT', zlib.deflateSync(raw)),
    chunk('IEND', Buffer.alloc(0)),
  ]);
}

function rgba16(fill) {
  const rgba = Buffer.alloc(16 * 16 * 4);
  for (let y = 0; y < 16; y++) {
    for (let x = 0; x < 16; x++) {
      const [r, g, b, a = 255] = fill(x, y);
      const i = (y * 16 + x) * 4;
      rgba[i] = r;
      rgba[i + 1] = g;
      rgba[i + 2] = b;
      rgba[i + 3] = a;
    }
  }
  return rgba;
}

function writePng(rel, rgba) {
  const file = path.join(root, rel);
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, png(16, 16, rgba));
}

const palette = {
  brass: [184, 133, 54],
  brassLight: [218, 174, 82],
  wood: [73, 45, 27],
  woodLight: [118, 75, 39],
  dark: [30, 31, 32],
  andesite: [101, 108, 104],
  ivory: [224, 211, 165],
  paper: [235, 225, 186],
  ink: [34, 30, 24],
  red: [162, 55, 42],
  green: [72, 138, 82],
};

function noise(color, x, y, amount = 8) {
  const n = ((x * 11 + y * 17 + (x ^ y) * 5) % (amount * 2 + 1)) - amount;
  return color.map((c) => Math.max(0, Math.min(255, c + n)));
}

function framed(base, accent) {
  return rgba16((x, y) => {
    let c = noise(base, x, y);
    if (x === 0 || y === 0 || x === 15 || y === 15) c = palette.dark;
    else if (x <= 2 || y <= 2 || x >= 13 || y >= 13) c = noise(accent, x, y);
    return [...c, 255];
  });
}

function blockTexture(id) {
  return rgba16((x, y) => {
    let c = noise(palette.wood, x, y);
    if (x === 0 || y === 0 || x === 15 || y === 15) c = palette.dark;
    else if (x <= 2 || y <= 2 || x >= 13 || y >= 13) c = noise(palette.brass, x, y);

    if (id === 'function_setting_drum') {
      if (y >= 5 && y <= 10 && x >= 3 && x <= 12) c = noise(palette.brassLight, x, y);
      if ((x === 5 || x === 8 || x === 11) && y >= 4 && y <= 11) c = palette.dark;
    }
    if (id === 'difference_register_column' || id === 'store_memory_column') {
      if (x >= 5 && x <= 10 && y >= 2 && y <= 13) c = noise(palette.ivory, x, y);
      if ((y === 4 || y === 8 || y === 12) && x >= 4 && x <= 11) c = palette.dark;
      if (id === 'difference_register_column' && x >= 11 && x <= 13 && y >= 4 && y <= 11) c = palette.brassLight;
    }
    if (id === 'table_printer' || id === 'mechanical_printer') {
      if (x >= 4 && x <= 12 && y >= 8 && y <= 12) c = noise(palette.paper, x, y);
      if (x >= 5 && x <= 11 && y >= 4 && y <= 6) c = palette.dark;
      if (id === 'mechanical_printer' && x >= 3 && x <= 5 && y >= 3 && y <= 5) c = palette.green;
    }
    if (id === 'punched_card_reader') {
      if (x >= 3 && x <= 12 && y >= 5 && y <= 10) c = noise(palette.paper, x, y);
      if ((x + y) % 4 === 0 && x >= 5 && x <= 10 && y >= 6 && y <= 9) c = palette.ink;
    }
    if (id === 'mill_arithmetic_block') {
      const dx = x - 8;
      const dy = y - 8;
      const d = dx * dx + dy * dy;
      if (d <= 30) c = noise(palette.andesite, x, y);
      if (d <= 16) c = noise(palette.brassLight, x, y);
      if (d <= 5) c = palette.dark;
    }
    return [...c, 255];
  });
}

function itemTexture(id) {
  return rgba16((x, y) => {
    let c = [0, 0, 0, 0];
    if (x >= 3 && x <= 12 && y >= 2 && y <= 13) c = [...noise(palette.paper, x, y), 255];
    if (x === 3 || x === 12 || y === 2 || y === 13) c = [...palette.ink, 255];
    if (id.includes('punched') && (x + y) % 4 === 0 && x >= 5 && x <= 10 && y >= 4 && y <= 11) c = [...palette.ink, 255];
    if (id.includes('curve') && x >= 5 && x <= 10 && y >= 5 && y <= 10 && (x - y === 0 || x + y === 15)) c = [...palette.red, 255];
    if (id.includes('calibration') && x >= 5 && x <= 10 && y >= 5 && y <= 10) c = [...palette.green, 255];
    return c;
  });
}

for (const id of blocks) {
  writeJson(`src/main/resources/assets/createaddon/blockstates/${id}.json`, {
    variants: { '': { model: `createaddon:block/${id}` } },
  });
  writeJson(`src/main/resources/assets/createaddon/models/block/${id}.json`, {
    parent: 'minecraft:block/cube_all',
    textures: { all: `createaddon:block/${id}` },
  });
  writeJson(`src/main/resources/assets/createaddon/models/item/${id}.json`, {
    parent: `createaddon:block/${id}`,
  });
  writeJson(`src/main/resources/data/createaddon/loot_table/blocks/${id}.json`, {
    type: 'minecraft:block',
    pools: [{
      rolls: 1,
      entries: [{ type: 'minecraft:item', name: `createaddon:${id}` }],
      conditions: [{ condition: 'minecraft:survives_explosion' }],
    }],
  });
  writePng(`src/main/resources/assets/createaddon/textures/block/${id}.png`, blockTexture(id));
}

for (const id of items) {
  writeJson(`src/main/resources/assets/createaddon/models/item/${id}.json`, {
    parent: 'minecraft:item/generated',
    textures: { layer0: `createaddon:item/${id}` },
  });
  writePng(`src/main/resources/assets/createaddon/textures/item/${id}.png`, itemTexture(id));
}

const recipe = (id, pattern, key) => writeJson(`src/main/resources/data/createaddon/recipe/${id}.json`, {
  type: 'minecraft:crafting_shaped',
  category: 'misc',
  pattern,
  key,
  result: { id: `createaddon:${id}`, count: 1 },
});

recipe('function_setting_drum', [' B ', 'ACA', ' B '], {
  B: { item: 'create:brass_ingot' },
  A: { item: 'create:andesite_alloy' },
  C: { item: 'create:shaft' },
});
recipe('difference_register_column', [' P ', 'BCB', ' P '], {
  P: { item: 'minecraft:paper' },
  B: { item: 'create:brass_ingot' },
  C: { item: 'create:cogwheel' },
});
recipe('table_printer', [' P ', 'BCB', ' R '], {
  P: { item: 'minecraft:piston' },
  B: { item: 'create:brass_ingot' },
  C: { item: 'create:brass_casing' },
  R: { item: 'minecraft:paper' },
});
recipe('punched_card_reader', ['PPP', 'BCB', 'ASA'], {
  P: { item: 'minecraft:paper' },
  B: { item: 'create:brass_ingot' },
  C: { item: 'create:brass_casing' },
  A: { item: 'create:andesite_alloy' },
  S: { item: 'create:shaft' },
});
recipe('mill_arithmetic_block', ['CGC', 'BPB', 'CGC'], {
  C: { item: 'create:cogwheel' },
  G: { item: 'create:large_cogwheel' },
  B: { item: 'create:brass_ingot' },
  P: { item: 'create:precision_mechanism' },
});
recipe('store_memory_column', [' P ', 'BCB', ' P '], {
  P: { item: 'minecraft:paper' },
  B: { item: 'create:brass_ingot' },
  C: { item: 'create:brass_casing' },
});
recipe('mechanical_printer', [' P ', 'BCB', ' M '], {
  P: { item: 'minecraft:piston' },
  B: { item: 'create:brass_ingot' },
  C: { item: 'create:brass_casing' },
  M: { item: 'create:precision_mechanism' },
});

writeJson('src/main/resources/data/createaddon/recipe/blank_table_paper.json', {
  type: 'minecraft:crafting_shapeless',
  category: 'misc',
  ingredients: [{ item: 'minecraft:paper' }, { item: 'minecraft:black_dye' }],
  result: { id: 'createaddon:blank_table_paper', count: 2 },
});
writeJson('src/main/resources/data/createaddon/recipe/blank_punched_card.json', {
  type: 'minecraft:crafting_shapeless',
  category: 'misc',
  ingredients: [{ item: 'minecraft:paper' }, { item: 'create:brass_nugget' }],
  result: { id: 'createaddon:blank_punched_card', count: 2 },
});

function updateLang(rel, entries) {
  const file = path.join(root, rel);
  const lang = JSON.parse(fs.readFileSync(file, 'utf8'));
  Object.assign(lang, entries);
  fs.writeFileSync(file, `${JSON.stringify(lang, null, 2)}\n`, 'utf8');
}

updateLang('src/main/resources/assets/createaddon/lang/en_us.json', {
  'block.createaddon.function_setting_drum': 'Function Setting Drum',
  'block.createaddon.difference_register_column': 'Difference Register Column',
  'block.createaddon.table_printer': 'Table Printer',
  'block.createaddon.punched_card_reader': 'Punched Card Reader',
  'block.createaddon.mill_arithmetic_block': 'Mill Arithmetic Block',
  'block.createaddon.store_memory_column': 'Store Memory Column',
  'block.createaddon.mechanical_printer': 'Mechanical Printer',
  'item.createaddon.blank_table_paper': 'Blank Table Paper',
  'item.createaddon.basic_processing_curve_table': 'Basic Processing Curve Table',
  'item.createaddon.blank_punched_card': 'Blank Punched Card',
  'item.createaddon.machine_calibration_card': 'Machine Calibration Card',
  'message.createaddon.computation.missing_components': 'Missing nearby component: %s (%s required, %s found)',
  'message.createaddon.computation.started_table': 'Difference engine started printing a processing curve table',
  'message.createaddon.computation.started_card': 'Analytical engine started compiling a machine calibration card',
  'message.createaddon.computation.busy': 'The computation assembly is still processing, or its output is waiting',
  'message.createaddon.computation.empty': 'No computation output is ready',
  'message.createaddon.computation.extracted': 'Retrieved: %s',
});

updateLang('src/main/resources/assets/createaddon/lang/zh_cn.json', {
  'block.createaddon.function_setting_drum': '函数设定鼓',
  'block.createaddon.difference_register_column': '差分寄存器柱',
  'block.createaddon.table_printer': '数表打印机',
  'block.createaddon.punched_card_reader': '穿孔卡读卡器',
  'block.createaddon.mill_arithmetic_block': 'Mill 运算磨坊',
  'block.createaddon.store_memory_column': 'Store 存储柱',
  'block.createaddon.mechanical_printer': '机械打印机',
  'item.createaddon.blank_table_paper': '空白数表纸',
  'item.createaddon.basic_processing_curve_table': '基础加工曲线表',
  'item.createaddon.blank_punched_card': '空白穿孔卡',
  'item.createaddon.machine_calibration_card': '机器校准卡',
  'message.createaddon.computation.missing_components': '缺少附近组件：%s（需要 %s 个，已有 %s 个）',
  'message.createaddon.computation.started_table': '差分机开始打印加工曲线表',
  'message.createaddon.computation.started_card': '分析机开始编译机器校准卡',
  'message.createaddon.computation.busy': '计算组件正在处理，或产物还未取出',
  'message.createaddon.computation.empty': '没有可取出的计算产物',
  'message.createaddon.computation.extracted': '已取出：%s',
});

console.log('Generated computation engine resources.');
