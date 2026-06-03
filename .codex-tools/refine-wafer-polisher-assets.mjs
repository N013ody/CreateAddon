import fs from 'node:fs';
import path from 'node:path';
import zlib from 'node:zlib';

const root = process.cwd();
const textureRoot = 'src/main/resources/assets/createaddon/textures/block';
const textureSize = 32;

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
    chunk('IEND', Buffer.alloc(0))
  ]);
}

function writePng(rel, rgba, width = textureSize, height = textureSize) {
  const file = path.join(root, rel);
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, png(width, height, rgba));
}

function pixelBuffer(fill, width = textureSize, height = textureSize) {
  const rgba = Buffer.alloc(width * height * 4);
  for (let y = 0; y < height; y++) {
    for (let x = 0; x < width; x++) {
      const [r, g, b, a = 255] = fill(x, y, width, height);
      const i = (y * width + x) * 4;
      rgba[i] = r;
      rgba[i + 1] = g;
      rgba[i + 2] = b;
      rgba[i + 3] = a;
    }
  }
  return rgba;
}

function clamp(value) {
  return Math.max(0, Math.min(255, Math.round(value)));
}

function tone(base, amount) {
  return base.map((channel) => clamp(channel + amount));
}

function rgba(color, alpha = 255) {
  return [...color, alpha];
}

function inRect(x, y, x0, y0, x1, y1) {
  return x >= x0 && x <= x1 && y >= y0 && y <= y1;
}

function frame(x, y, size = textureSize) {
  return x === 0 || y === 0 || x === size - 1 || y === size - 1;
}

function unit(value, size = textureSize) {
  return value * size / 16;
}

function inUnitRect(x, y, x0, y0, x1, y1, width = textureSize, height = textureSize) {
  return x >= unit(x0, width) && x < unit(x1 + 1, width)
    && y >= unit(y0, height) && y < unit(y1 + 1, height);
}

function unitDistance(x, y, cx, cy, width = textureSize, height = textureSize) {
  const dx = x - unit(cx, width);
  const dy = y - unit(cy, height);
  return dx * dx + dy * dy;
}

function panelShade(base, x, y, noise = 0) {
  const light = (x < textureSize / 4 ? 7 : 0) + (y < textureSize / 4 ? 9 : 0) - (y > textureSize * 0.72 ? 12 : 0);
  const pixelNoise = noise ? (((x * 13 + y * 17 + x * y) % (noise * 2 + 1)) - noise) : 0;
  return tone(base, light + pixelNoise);
}

function bolt(x, y, px, py, color) {
  return Math.abs(x - px) <= 1 && Math.abs(y - py) <= 1 ? color : undefined;
}

const palette = {
  outline: [86, 91, 91],
  shadow: [111, 119, 120],
  graphite: [139, 148, 148],
  graphiteLight: [181, 188, 186],
  graphiteDark: [83, 91, 94],
  brass: [226, 224, 211],
  brassLight: [248, 247, 238],
  brassDark: [178, 176, 162],
  copper: [164, 170, 164],
  copperDark: [103, 112, 113],
  rubber: [47, 51, 52],
  rubberLight: [82, 88, 88],
  ceramic: [239, 241, 232],
  wafer: [191, 226, 226],
  waferBlue: [117, 191, 197],
  cyan: [70, 185, 194],
  amber: [226, 214, 150]
};

function bodyTexture() {
  return pixelBuffer((x, y, w, h) => {
    let color = panelShade(palette.brass, x, y, 1);
    if (frame(x, y, w)) color = tone(palette.brassDark, 10);
    if (inUnitRect(x, y, 2, 2, 13, 13, w, h)) color = panelShade(palette.brass, x, y, 1);
    if (inUnitRect(x, y, 3, 3, 12, 3, w, h) || inUnitRect(x, y, 3, 12, 12, 12, w, h)) color = palette.brassDark;
    return rgba(color);
  });
}

function frameTexture() {
  return pixelBuffer((x, y, w, h) => {
    let color = panelShade(tone(palette.graphite, 2), x, y, 1);
    if (frame(x, y, w)) color = palette.graphiteDark;
    if (inUnitRect(x, y, 2, 2, 13, 13, w, h)) color = panelShade(tone(palette.graphiteLight, -10), x, y, 1);
    if (inUnitRect(x, y, 3, 7, 12, 8, w, h)) color = tone(palette.graphite, 18);
    return rgba(color);
  });
}

function driveTexture() {
  return pixelBuffer((x, y, w, h) => {
    let color = palette.graphiteDark;
    const d = unitDistance(x, y, 7.5, 7.5, w, h);
    if (frame(x, y, w)) color = palette.graphiteDark;
    if (inUnitRect(x, y, 2, 2, 13, 13, w, h)) color = tone(palette.graphite, -3);
    if (d <= 145) color = tone(palette.graphiteLight, -12);
    if (d <= 95) color = palette.rubber;
    if (d <= 44) color = palette.shadow;
    return rgba(color);
  });
}

function chainTexture() {
  return pixelBuffer((x, y, w, h) => {
    let color = tone(palette.graphiteDark, -2);
    if (inUnitRect(x, y, 2, 5, 13, 10, w, h)) color = tone(palette.graphite, -8);
    if (inUnitRect(x, y, 3, 7, 12, 8, w, h)) color = tone(palette.graphiteLight, -18);
    return rgba(color);
  });
}

function topTexture() {
  return pixelBuffer((x, y, w, h) => {
    let color = panelShade(palette.brassLight, x, y, 1);
    const d = unitDistance(x, y, 7.5, 7.5, w, h);
    if (frame(x, y, w)) color = palette.brassDark;
    if (d <= 175) color = tone(palette.graphiteLight, -2);
    if (d <= 134) color = palette.rubber;
    if (d <= 82) color = palette.waferBlue;
    if (d <= 58) color = palette.wafer;
    if (d <= 100 && d >= 92) color = tone(palette.ceramic, -10);
    return rgba(color);
  });
}

function frontTexture() {
  return pixelBuffer((x, y, w, h) => {
    let color = palette.brassDark;
    if (frame(x, y, w)) color = palette.outline;
    if (inUnitRect(x, y, 2, 2, 13, 13, w, h)) color = panelShade(palette.brass, x, y, 1);
    if (inUnitRect(x, y, 3, 5, 12, 10, w, h)) color = palette.shadow;
    if (inUnitRect(x, y, 4, 6, 11, 9, w, h)) color = palette.ceramic;
    if (inUnitRect(x, y, 5, 7, 10, 8, w, h)) color = palette.waferBlue;
    if (inUnitRect(x, y, 2, 12, 13, 13, w, h)) color = palette.graphiteDark;
    if (inUnitRect(x, y, 4, 11, 11, 11, w, h)) color = palette.brassDark;
    return rgba(color);
  });
}

function detailTexture() {
  return pixelBuffer((x, y, w, h) => {
    let color = palette.outline;
    const d = unitDistance(x, y, 7.5, 7.5, w, h);
    if (d <= 190) color = tone(palette.graphiteLight, -8);
    if (d <= 150) color = palette.rubberLight;
    if (d <= 112) color = palette.rubber;
    if (d <= 36) color = palette.shadow;
    if (d <= 132 && d >= 126) color = tone(palette.graphiteLight, 12);
    if (frame(x, y, w)) color = palette.outline;
    return rgba(color);
  });
}

function accentTexture() {
  return pixelBuffer((x, y, w, h) => {
    let color = panelShade(palette.ceramic, x, y, 1);
    if (frame(x, y, w)) color = palette.brassDark;
    if (inUnitRect(x, y, 4, 4, 11, 11, w, h)) color = tone(palette.graphiteLight, -8);
    if (inUnitRect(x, y, 6, 6, 9, 9, w, h)) color = palette.rubber;
    return rgba(color);
  });
}

function sheetTexture() {
  return pixelBuffer((x, y, w, h) => {
    let color = palette.graphiteDark;
    if (inUnitRect(x, y, 0, 0, 7, 7, w, h)) color = panelShade(palette.brass, x, y, 1);
    if (inUnitRect(x, y, 8, 0, 15, 7, w, h)) color = panelShade(tone(palette.graphiteLight, -12), x, y, 1);
    if (inUnitRect(x, y, 0, 8, 7, 15, w, h)) color = palette.rubber;
    if (inUnitRect(x, y, 8, 8, 15, 15, w, h)) color = tone(palette.graphite, -6);
    if (x === 0 || x === unit(7, w) || x === unit(8, w) || x === w - 1 || y === 0 || y === unit(7, h) || y === unit(8, h) || y === h - 1) {
      color = palette.graphiteDark;
    }
    if (inUnitRect(x, y, 3, 3, 4, 4, w, h)) color = palette.brassLight;
    if (inUnitRect(x, y, 11, 3, 12, 4, w, h)) color = tone(palette.graphiteLight, 4);
    if (inUnitRect(x, y, 3, 11, 4, 12, w, h)) color = tone(palette.cyan, -12);
    if (inUnitRect(x, y, 11, 11, 12, 12, w, h)) color = tone(palette.graphiteDark, 12);
    return rgba(color);
  });
}

function face(texture, uv = [0, 0, 16, 16]) {
  return { uv, texture };
}

function faces(texture, overrides = {}) {
  return Object.fromEntries(
    ['north', 'east', 'south', 'west', 'up', 'down'].map((side) => {
      const value = overrides[side] ?? texture;
      return [side, Array.isArray(value) ? face(value[0], value[1]) : face(value)];
    })
  );
}

function box(name, from, to, faceMap) {
  return { name, from, to, faces: faceMap };
}

const model = {
  credit: 'CreateAddon CMP wafer polisher v7; white minimalist 32px texture set',
  parent: 'block/block',
  ambientocclusion: true,
  textures: {
    particle: 'createaddon:block/wafer_polisher_front',
    body: 'createaddon:block/wafer_polisher_body',
    frame: 'createaddon:block/wafer_polisher_frame',
    drive: 'createaddon:block/wafer_polisher_drive',
    chain: 'createaddon:block/wafer_polisher_chain',
    sheet: 'createaddon:block/wafer_polisher_sheet',
    top: 'createaddon:block/wafer_polisher_top',
    front: 'createaddon:block/wafer_polisher_front',
    detail: 'createaddon:block/wafer_polisher_detail',
    accent: 'createaddon:block/wafer_polisher_accent'
  },
  elements: [
    box('low_graphite_base', [1, 0, 1], [15, 2, 15], faces('#frame')),
    box('compact_precision_body', [2, 2, 2], [14, 5, 14], faces('#body', {
      up: '#sheet',
      north: '#front',
      down: '#frame'
    })),
    box('front_wafer_drawer', [4, 2, 0], [12, 5, 2], faces('#front', {
      up: '#sheet',
      down: '#frame'
    })),
    box('left_custom_drive_box', [0, 2, 5], [2, 9, 11], faces('#drive', {
      east: '#frame',
      up: '#drive',
      down: '#frame'
    })),
    box('right_slurry_service_box', [14, 3, 6], [16, 8, 10], faces('#frame', {
      west: '#sheet',
      east: '#accent',
      up: '#body'
    })),
    box('rear_left_press_column', [3, 5, 12], [5, 12, 14], faces('#frame', {
      up: '#drive',
      north: '#sheet'
    })),
    box('rear_right_press_column', [11, 5, 12], [13, 12, 14], faces('#frame', {
      up: '#drive',
      north: '#sheet'
    })),
    box('overhead_pressure_beam', [5, 12, 9], [11, 14, 14], faces('#body', {
      north: '#chain',
      south: '#chain',
      down: '#frame'
    })),
    box('round_polishing_platen', [4, 5, 4], [12, 6, 12], faces('#detail', {
      up: '#top',
      down: '#frame'
    })),
    box('raised_wafer_stage', [5, 6, 5], [11, 7, 11], faces('#top', {
      down: '#detail'
    })),
    box('pressure_head_carriage', [6, 9, 6], [10, 11, 10], faces('#accent', {
      up: '#body',
      north: '#sheet',
      south: '#sheet'
    })),
    box('small_pressure_pad', [7, 8, 7], [9, 9, 9], faces('#detail', {
      up: '#accent',
      down: '#detail'
    })),
  ],
  groups: [
    {
      name: 'cmp_polisher_body',
      origin: [8, 8, 8],
      color: 0,
      children: Array.from({ length: 12 }, (_, index) => index)
    }
  ]
};

const textures = {
  wafer_polisher_body: bodyTexture(),
  wafer_polisher_frame: frameTexture(),
  wafer_polisher_drive: driveTexture(),
  wafer_polisher_chain: chainTexture(),
  wafer_polisher_top: topTexture(),
  wafer_polisher_front: frontTexture(),
  wafer_polisher_detail: detailTexture(),
  wafer_polisher_accent: accentTexture(),
  wafer_polisher_sheet: sheetTexture()
};

function reviewSheet(textureEntries, scale = 4, columns = 3) {
  const cell = textureSize * scale;
  const gap = 8;
  const rows = Math.ceil(textureEntries.length / columns);
  const width = columns * cell + (columns + 1) * gap;
  const height = rows * cell + (rows + 1) * gap;
  const out = Buffer.alloc(width * height * 4, 255);

  for (let i = 0; i < textureEntries.length; i++) {
    const [, source] = textureEntries[i];
    const col = i % columns;
    const row = Math.floor(i / columns);
    const ox = gap + col * (cell + gap);
    const oy = gap + row * (cell + gap);
    for (let sy = 0; sy < textureSize; sy++) {
      for (let sx = 0; sx < textureSize; sx++) {
        const si = (sy * textureSize + sx) * 4;
        for (let py = 0; py < scale; py++) {
          for (let px = 0; px < scale; px++) {
            const dx = ox + sx * scale + px;
            const dy = oy + sy * scale + py;
            const di = (dy * width + dx) * 4;
            out[di] = source[si];
            out[di + 1] = source[si + 1];
            out[di + 2] = source[si + 2];
            out[di + 3] = 255;
          }
        }
      }
    }
  }

  return { rgba: out, width, height };
}

writeJson('src/main/resources/assets/createaddon/models/block/wafer_polisher.json', model);
for (const [name, data] of Object.entries(textures)) {
  writePng(`${textureRoot}/${name}.png`, data);
}
writePng(`${textureRoot}/wafer_polisher.png`, textures.wafer_polisher_front);

const review = reviewSheet(Object.entries(textures));
writePng('.codex-tools/wafer-polisher-texture-review.png', review.rgba, review.width, review.height);

console.log('Generated v7 white minimalist 32px wafer polisher model, textures, and review sheet');
