import fs from 'node:fs';
import path from 'node:path';
import zlib from 'node:zlib';

const root = process.cwd();
const textureRoot = path.join(root, 'src/main/resources/assets/createaddon/textures');
const outPath = path.join(root, '.codex-tools/chip-production-preview.png');
const machines = [
  'crystal_pull_furnace',
  'wafer_saw',
  'wafer_polisher',
  'oxidation_diffusion_furnace',
  'photolithography_table',
  'packaging_test_bench'
];
const entries = [
  ...machines.flatMap((name) => [
    `block/${name}.png`,
    `block/${name}_top.png`,
    `block/${name}_front.png`,
    `block/${name}_detail.png`,
    `block/${name}_accent.png`
  ]),
  'item/silica_dust.png',
  'item/raw_silicon.png',
  'item/silicon_boule.png',
  'item/rough_wafer.png',
  'item/polished_wafer.png',
  'item/oxidized_wafer.png',
  'item/photoresist_wafer.png',
  'item/etched_wafer.png',
  'item/doped_wafer.png',
  'item/metallized_wafer.png',
  'item/finished_wafer.png',
  'item/bare_logic_die.png',
  'item/ceramic_chip_package.png',
  'item/basic_logic_chip.png'
];

function crc32(buf) {
  let crc = 0xffffffff;
  for (const byte of buf) {
    crc ^= byte;
    for (let i = 0; i < 8; i++) crc = (crc >>> 1) ^ (0xedb88320 & -(crc & 1));
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

function encode(width, height, rgba) {
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
  return Buffer.concat([Buffer.from('89504e470d0a1a0a', 'hex'), chunk('IHDR', ihdr), chunk('IDAT', zlib.deflateSync(raw)), chunk('IEND', Buffer.alloc(0))]);
}

function decode(file) {
  const bytes = fs.readFileSync(file);
  let offset = 8;
  let width = 0;
  let height = 0;
  const idat = [];
  while (offset < bytes.length) {
    const len = bytes.readUInt32BE(offset);
    offset += 4;
    const type = bytes.subarray(offset, offset + 4).toString('ascii');
    offset += 4;
    const data = bytes.subarray(offset, offset + len);
    offset += len + 4;
    if (type === 'IHDR') {
      width = data.readUInt32BE(0);
      height = data.readUInt32BE(4);
    }
    if (type === 'IDAT') idat.push(data);
    if (type === 'IEND') break;
  }
  const inflated = zlib.inflateSync(Buffer.concat(idat));
  const rgba = Buffer.alloc(width * height * 4);
  const stride = width * 4 + 1;
  for (let y = 0; y < height; y++) inflated.copy(rgba, y * width * 4, y * stride + 1, y * stride + 1 + width * 4);
  return { width, height, rgba };
}

function fill(buf, width, x0, y0, w, h, color) {
  for (let y = y0; y < y0 + h; y++) {
    for (let x = x0; x < x0 + w; x++) {
      const i = (y * width + x) * 4;
      buf[i] = color[0];
      buf[i + 1] = color[1];
      buf[i + 2] = color[2];
      buf[i + 3] = color[3];
    }
  }
}

const scale = 8;
const tile = 16 * scale;
const gap = 14;
const cols = 5;
const rows = Math.ceil(entries.length / cols);
const width = cols * tile + (cols + 1) * gap;
const height = rows * tile + (rows + 1) * gap;
const sheet = Buffer.alloc(width * height * 4);
fill(sheet, width, 0, 0, width, height, [31, 33, 35, 255]);

for (let idx = 0; idx < entries.length; idx++) {
  const image = decode(path.join(textureRoot, entries[idx]));
  const col = idx % cols;
  const row = Math.floor(idx / cols);
  const ox = gap + col * (tile + gap);
  const oy = gap + row * (tile + gap);
  fill(sheet, width, ox - 2, oy - 2, tile + 4, tile + 4, [72, 75, 78, 255]);
  for (let y = 0; y < image.height; y++) {
    for (let x = 0; x < image.width; x++) {
      const src = (y * image.width + x) * 4;
      const a = image.rgba[src + 3];
      if (!a) continue;
      fill(sheet, width, ox + x * scale, oy + y * scale, scale, scale, [
        image.rgba[src],
        image.rgba[src + 1],
        image.rgba[src + 2],
        a
      ]);
    }
  }
}

fs.writeFileSync(outPath, encode(width, height, sheet));
console.log(outPath);
