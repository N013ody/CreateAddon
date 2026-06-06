import fs from 'node:fs';
import path from 'node:path';
import zlib from 'node:zlib';

const root = process.cwd();

const palette = {
  dark: [28, 28, 29],
  edge: [48, 39, 27],
  brass: [190, 135, 45],
  brassLight: [232, 190, 82],
  copper: [167, 83, 52],
  wood: [91, 58, 31],
  paper: [232, 222, 184],
  ink: [36, 32, 24],
  green: [70, 145, 82],
  blue: [77, 118, 145],
  red: [166, 58, 45],
};

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

function noise(color, x, y, amount = 8) {
  const n = ((x * 13 + y * 19 + (x ^ y) * 7) % (amount * 2 + 1)) - amount;
  return color.map((c) => Math.max(0, Math.min(255, c + n)));
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

function frame(base, accent) {
  return (x, y) => {
    let c = noise(base, x, y);
    if (x === 0 || y === 0 || x === 15 || y === 15) c = palette.dark;
    else if (x <= 2 || y <= 2 || x >= 13 || y >= 13) c = noise(accent, x, y);
    return c;
  };
}

function powerEnd(kind) {
  const base = kind === 'table_printer' ? palette.wood : palette.copper;
  const accent = kind === 'table_printer' ? palette.brass : palette.brassLight;
  return rgba16((x, y) => {
    let c = frame(base, accent)(x, y);
    const dx = x - 7.5;
    const dy = y - 7.5;
    const d = dx * dx + dy * dy;
    if (d <= 38) c = palette.dark;
    if (d <= 27) c = noise(palette.brassLight, x, y, 5);
    if (d <= 12) c = palette.dark;
    if ((x === 7 || x === 8 || y === 7 || y === 8) && d <= 30) c = noise(palette.brass, x, y, 4);
    if (x >= 6 && x <= 9 && y >= 6 && y <= 9) c = palette.dark;
    return [...c, 255];
  });
}

function operationSide(kind) {
  const base = kind === 'table_printer' ? palette.wood : palette.copper;
  const accent = kind === 'table_printer' ? palette.brass : palette.brassLight;
  return rgba16((x, y) => {
    let c = frame(base, accent)(x, y);
    if (x >= 3 && x <= 12 && y >= 4 && y <= 6) c = palette.dark;
    if (x >= 4 && x <= 11 && y >= 8 && y <= 12) c = noise(palette.paper, x, y, 5);
    if (kind === 'mechanical_printer' && x >= 5 && x <= 10 && y >= 9 && y <= 11) c = noise(palette.green, x, y, 4);
    if (kind === 'table_printer' && ((x - 4 === y - 8) || (x + y === 19)) && x >= 4 && x <= 11 && y >= 8 && y <= 12) c = palette.red;
    if (x >= 12 && x <= 13 && y >= 8 && y <= 12) c = palette.ink;
    return [...c, 255];
  });
}

function statusTop(kind) {
  const base = kind === 'table_printer' ? palette.wood : palette.copper;
  const accent = kind === 'table_printer' ? palette.brass : palette.brassLight;
  return rgba16((x, y) => {
    let c = frame(base, accent)(x, y);
    if (x >= 4 && x <= 11 && y >= 4 && y <= 11) c = kind === 'table_printer' ? noise(palette.blue, x, y, 4) : noise(palette.green, x, y, 4);
    if (x >= 5 && x <= 10 && (y === 6 || y === 9)) c = palette.dark;
    if (y >= 5 && y <= 10 && (x === 6 || x === 9)) c = palette.dark;
    return [...c, 255];
  });
}

function writeModel(id) {
  const model = {
    parent: 'minecraft:block/cube',
    textures: {
      particle: `createaddon:block/${id}_side`,
      down: `createaddon:block/${id}_end`,
      up: `createaddon:block/${id}_end`,
      north: `createaddon:block/${id}_side`,
      south: `createaddon:block/${id}_side`,
      west: `createaddon:block/${id}_side`,
      east: `createaddon:block/${id}_status`,
    },
  };
  fs.writeFileSync(path.join(root, `src/main/resources/assets/createaddon/models/block/${id}.json`),
    `${JSON.stringify(model, null, 2)}\n`, 'utf8');
}

for (const id of ['table_printer', 'mechanical_printer']) {
  writePng(`src/main/resources/assets/createaddon/textures/block/${id}_end.png`, powerEnd(id));
  writePng(`src/main/resources/assets/createaddon/textures/block/${id}_side.png`, operationSide(id));
  writePng(`src/main/resources/assets/createaddon/textures/block/${id}_status.png`, statusTop(id));
  writeModel(id);
}

console.log('Marked computation machine faces.');
