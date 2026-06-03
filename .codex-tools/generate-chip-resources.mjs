import fs from 'node:fs';
import path from 'node:path';
import zlib from 'node:zlib';

const root = process.cwd();

const machines = [
  'crystal_pull_furnace',
  'wafer_saw',
  'wafer_polisher',
  'oxidation_diffusion_furnace',
  'photolithography_table',
  'packaging_test_bench'
];

const items = [
  'silica_dust',
  'raw_silicon',
  'silicon_boule',
  'rough_wafer',
  'polished_wafer',
  'oxidized_wafer',
  'photoresist_wafer',
  'etched_wafer',
  'doped_wafer',
  'metallized_wafer',
  'finished_wafer',
  'bare_logic_die',
  'ceramic_chip_package',
  'basic_logic_chip'
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
    chunk('IEND', Buffer.alloc(0))
  ]);
}

function pixelBuffer(fill) {
  const rgba = Buffer.alloc(16 * 16 * 4);
  for (let y = 0; y < 16; y++) {
    for (let x = 0; x < 16; x++) {
      const [r, g, b, a] = fill(x, y);
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

function withAlpha(color, alpha = 255) {
  return [...color, alpha];
}

function shade(base, x, y) {
  const n = ((x * 13 + y * 17 + (x ^ y) * 3) % 11) - 5;
  return base.map((channel) => Math.max(0, Math.min(255, channel + n)));
}

function machineTexture(name) {
  const palette = {
    crystal_pull_furnace: { body: [152, 102, 54], trim: [205, 154, 77], dark: [48, 43, 39], accent: [242, 112, 35], glass: [139, 211, 222] },
    wafer_saw: { body: [97, 103, 104], trim: [178, 145, 82], dark: [42, 47, 49], accent: [191, 219, 225], glass: [92, 131, 143] },
    wafer_polisher: { body: [129, 89, 48], trim: [214, 165, 80], dark: [44, 42, 40], accent: [204, 210, 202], glass: [115, 169, 170] },
    oxidation_diffusion_furnace: { body: [115, 78, 50], trim: [197, 118, 67], dark: [35, 33, 32], accent: [255, 126, 45], glass: [164, 217, 227] },
    photolithography_table: { body: [69, 72, 78], trim: [205, 159, 74], dark: [27, 30, 37], accent: [154, 104, 230], glass: [99, 168, 223] },
    packaging_test_bench: { body: [84, 86, 72], trim: [207, 157, 73], dark: [35, 37, 31], accent: [72, 153, 94], glass: [226, 214, 183] }
  }[name];

  return pixelBuffer((x, y) => {
    let color = shade(palette.body, x, y);
    if (x <= 1 || x >= 14 || y <= 1 || y >= 14) color = palette.trim;
    if (y >= 12) color = shade(palette.dark, x, y);
    if (name === 'crystal_pull_furnace' && x >= 6 && x <= 9 && y >= 2 && y <= 10) color = y > 7 ? palette.accent : palette.glass;
    if (name === 'wafer_saw' && (x === y || x + y === 15) && x >= 4 && x <= 11) color = palette.accent;
    if (name === 'wafer_polisher') {
      const d = (x - 8) * (x - 8) + (y - 7) * (y - 7);
      if (d >= 12 && d <= 28) color = palette.accent;
      if (d < 9) color = palette.glass;
    }
    if (name === 'oxidation_diffusion_furnace' && x >= 4 && x <= 11 && y >= 4 && y <= 8) color = (x + y) % 2 === 0 ? palette.accent : palette.glass;
    if (name === 'photolithography_table' && x >= 5 && x <= 10 && y >= 3 && y <= 8) color = y < 6 ? palette.glass : palette.accent;
    if (name === 'packaging_test_bench') {
      if (x >= 4 && x <= 11 && y >= 4 && y <= 10) color = palette.accent;
      if ((x === 5 || x === 10) && y >= 5 && y <= 9) color = palette.trim;
      if (x >= 6 && x <= 9 && y >= 6 && y <= 8) color = palette.glass;
    }
    return withAlpha(color);
  });
}

const refinedMachinePalettes = {
  crystal_pull_furnace: { frame: [154, 96, 52], dark: [43, 33, 29], metal: [184, 135, 76], glow: [238, 91, 32], glass: [122, 203, 211], ink: [23, 21, 20] },
  wafer_saw: { frame: [104, 112, 110], dark: [34, 39, 41], metal: [184, 195, 195], glow: [197, 224, 230], glass: [92, 150, 159], ink: [22, 25, 26] },
  wafer_polisher: { frame: [171, 128, 63], dark: [49, 43, 35], metal: [204, 200, 185], glow: [129, 183, 178], glass: [199, 214, 210], ink: [25, 23, 20] },
  oxidation_diffusion_furnace: { frame: [166, 91, 52], dark: [40, 31, 29], metal: [185, 116, 71], glow: [244, 111, 39], glass: [105, 208, 220], ink: [22, 20, 19] },
  photolithography_table: { frame: [181, 137, 62], dark: [37, 36, 42], metal: [92, 100, 112], glow: [126, 101, 226], glass: [94, 169, 223], ink: [22, 24, 31] },
  packaging_test_bench: { frame: [178, 135, 62], dark: [35, 39, 31], metal: [221, 207, 176], glow: [65, 151, 93], glass: [34, 99, 69], ink: [22, 25, 19] }
};

function edgeFrame(palette, x, y) {
  if (x === 0 || y === 0 || x === 15 || y === 15) return shade(palette.dark, x, y);
  if (x <= 2 || y <= 2 || x >= 13 || y >= 13) return shade(palette.frame, x, y);
  return undefined;
}

function inRect(x, y, x0, y0, x1, y1) {
  return x >= x0 && x <= x1 && y >= y0 && y <= y1;
}

function inDisc(x, y, cx, cy, r2) {
  const dx = x - cx;
  const dy = y - cy;
  return dx * dx + dy * dy <= r2;
}

function refinedMachineTexture(name, slot) {
  const p = refinedMachinePalettes[name];
  return pixelBuffer((x, y) => {
    let color = edgeFrame(p, x, y) ?? shade(slot === 'front' ? p.dark : p.frame, x, y);

    if (slot === 'top') {
      if (inRect(x, y, 3, 3, 12, 12)) color = shade(p.dark, x, y);
      if (inRect(x, y, 4, 4, 11, 11)) color = shade(p.metal, x, y);
      if (name === 'crystal_pull_furnace') {
        if (inRect(x, y, 6, 3, 9, 12)) color = p.glass;
        if (inRect(x, y, 6, 10, 9, 12)) color = p.glow;
      }
      if (name === 'wafer_saw') {
        if (inRect(x, y, 3, 7, 12, 8)) color = p.ink;
        if ((x === y || x + y === 15) && inRect(x, y, 4, 4, 11, 11)) color = p.glow;
        if (inRect(x, y, 6, 5, 9, 10)) color = p.glass;
      }
      if (name === 'wafer_polisher') {
        if (inDisc(x, y, 8, 8, 37)) color = p.metal;
        if (inDisc(x, y, 8, 8, 20)) color = p.glass;
        if (inDisc(x, y, 8, 8, 8)) color = p.dark;
      }
      if (name === 'oxidation_diffusion_furnace') {
        if (inRect(x, y, 4, 5, 11, 10)) color = (x + y) % 2 ? p.glass : p.glow;
        if (inRect(x, y, 3, 7, 12, 8)) color = p.ink;
      }
      if (name === 'photolithography_table') {
        if (inDisc(x, y, 8, 8, 25)) color = p.glass;
        if (inRect(x, y, 5, 5, 10, 10)) color = p.glow;
        if (x === 8 || y === 8) color = p.metal;
      }
      if (name === 'packaging_test_bench') {
        if (inRect(x, y, 4, 4, 11, 11)) color = p.glass;
        if ((x === 5 || x === 10 || y === 5 || y === 10) && inRect(x, y, 4, 4, 11, 11)) color = p.glow;
        if (inRect(x, y, 6, 6, 9, 9)) color = p.metal;
      }
    }

    if (slot === 'front') {
      if (inRect(x, y, 3, 3, 12, 12)) color = p.ink;
      if (inRect(x, y, 4, 4, 11, 11)) color = shade(p.dark, x, y);
      if (name.includes('furnace')) {
        if (inRect(x, y, 5, 5, 10, 10)) color = ((x + y) % 2 ? p.glow : p.dark);
        if (inRect(x, y, 6, 6, 9, 9)) color = p.glow;
      }
      if (name === 'wafer_saw') {
        if (inRect(x, y, 4, 6, 11, 9)) color = p.ink;
        if (inRect(x, y, 5, 7, 10, 8)) color = p.glass;
        if (x === 4 || x === 11) color = p.metal;
      }
      if (name === 'wafer_polisher') {
        if (inDisc(x, y, 8, 8, 28)) color = p.glass;
        if (inDisc(x, y, 8, 8, 12)) color = p.metal;
      }
      if (name === 'photolithography_table') {
        if (inRect(x, y, 5, 4, 10, 8)) color = p.glass;
        if (inRect(x, y, 5, 9, 10, 11)) color = p.glow;
      }
      if (name === 'packaging_test_bench') {
        if (inRect(x, y, 4, 4, 11, 10)) color = p.glass;
        if ((x + y) % 4 === 0 && inRect(x, y, 5, 5, 10, 9)) color = p.glow;
        if (inRect(x, y, 4, 11, 11, 12)) color = p.metal;
      }
    }

    if (slot === 'detail') {
      color = shade(p.dark, x, y);
      if (name === 'wafer_saw') {
        if (inRect(x, y, 6, 0, 9, 15)) color = shade(p.metal, x, y);
        if ((x + y) % 3 === 0 && inRect(x, y, 6, 1, 9, 14)) color = p.glow;
      } else if (name === 'photolithography_table') {
        if (inDisc(x, y, 8, 8, 30)) color = p.glass;
        if (inDisc(x, y, 8, 8, 14)) color = p.glow;
        if (x === 8 || y === 8) color = p.metal;
      } else if (name === 'packaging_test_bench') {
        if (inRect(x, y, 3, 4, 12, 11)) color = p.metal;
        if (inRect(x, y, 5, 6, 10, 9)) color = p.glass;
        if ((x === 4 || x === 11) && inRect(x, y, 5, 5, 10, 10)) color = p.glow;
      } else if (name === 'wafer_polisher') {
        if (inDisc(x, y, 8, 8, 39)) color = p.metal;
        if (inDisc(x, y, 8, 8, 20)) color = p.glass;
        if (inDisc(x, y, 8, 8, 6)) color = p.ink;
      } else {
        if (inRect(x, y, 4, 2, 11, 13)) color = p.glass;
        if (inRect(x, y, 6, 4, 9, 11)) color = p.glow;
        if (x === 5 || x === 10) color = p.metal;
      }
    }

    if (slot === 'accent') {
      color = shade(p.metal, x, y);
      if (x === 0 || y === 0 || x === 15 || y === 15) color = p.ink;
      if (inRect(x, y, 2, 2, 13, 13) && (x + y) % 5 === 0) color = p.glow;
      if (inRect(x, y, 4, 4, 11, 11)) color = shade(p.dark, x, y);
      if ((x === 4 || x === 11 || y === 4 || y === 11) && inRect(x, y, 4, 4, 11, 11)) color = p.metal;
      if (inRect(x, y, 6, 6, 9, 9)) color = p.glow;
    }

    return withAlpha(color);
  });
}

function face(texture, uv = [0, 0, 16, 16], extra = {}) {
  return { uv, texture, ...extra };
}

function box(name, from, to, faces) {
  return {
    name,
    from,
    to,
    faces: Object.fromEntries(
      Object.entries(faces).map(([side, texture]) => [
        side,
        Array.isArray(texture) ? face(texture[0], texture[1], texture[2] ?? {}) : face(texture)
      ])
    )
  };
}

const allFaces = (texture, top = texture, bottom = texture) => ({
  north: texture,
  east: texture,
  south: texture,
  west: texture,
  up: top,
  down: bottom
});

const createStyleBaseTextures = {
  crystal_pull_furnace: {
    particle: 'create:block/copper_casing',
    casing: 'create:block/copper_casing',
    casing_short: 'create:block/copper_casing',
    gearbox: 'create:block/gearbox',
    gearbox_top: 'create:block/gearbox_top',
    pipe: 'create:block/encased_pipe'
  },
  wafer_saw: {
    particle: 'create:block/andesite_casing',
    casing: 'create:block/andesite_casing',
    casing_short: 'create:block/andesite_casing_short',
    gearbox: 'create:block/gearbox',
    gearbox_top: 'create:block/gearbox_top',
    drive: 'create:block/encased_chain_drive'
  },
  wafer_polisher: {
    particle: 'create:block/brass_casing',
    casing: 'create:block/brass_casing',
    casing_short: 'create:block/andesite_casing_short',
    gearbox: 'create:block/gearbox',
    gearbox_top: 'create:block/gearbox_top',
    basin: 'create:block/basin'
  },
  oxidation_diffusion_furnace: {
    particle: 'create:block/copper_casing',
    casing: 'create:block/copper_casing',
    casing_short: 'create:block/copper_casing',
    gearbox: 'create:block/gearbox',
    gearbox_top: 'create:block/gearbox_top',
    pipe: 'create:block/encased_pipe',
    fan: 'create:block/fan_side'
  },
  photolithography_table: {
    particle: 'create:block/brass_casing',
    casing: 'create:block/brass_casing',
    casing_short: 'create:block/brass_casing',
    gearbox: 'create:block/gearbox',
    gearbox_top: 'create:block/gearbox_top',
    press_top: 'create:block/mechanical_press_top'
  },
  packaging_test_bench: {
    particle: 'create:block/brass_casing',
    casing: 'create:block/brass_casing',
    casing_short: 'create:block/andesite_casing_short',
    gearbox: 'create:block/gearbox',
    gearbox_top: 'create:block/gearbox_top',
    press_top: 'create:block/mechanical_press_top'
  }
};

function createStyleTextures(name) {
  return {
    ...createStyleBaseTextures[name],
    top: `createaddon:block/${name}_top`,
    front: `createaddon:block/${name}_front`,
    detail: `createaddon:block/${name}_detail`,
    accent: `createaddon:block/${name}_accent`
  };
}

function createStyleMachineModel(name) {
  const textures = createStyleTextures(name);
  const model = {
    credit: 'Generated for CreateAddon; styled after Create machine block composition',
    parent: 'create:block/block',
    textures,
    elements: []
  };

  const base = (topTexture = '#top') => [
    box('base_plate', [0, 0, 0], [16, 3, 16], {
      ...allFaces('#casing_short', topTexture, '#casing')
    }),
    box('rear_gearbox_face', [2, 3, 0], [14, 13, 2], {
      north: '#gearbox',
      east: '#gearbox_top',
      south: '#casing',
      west: '#gearbox_top',
      up: '#casing_short',
      down: '#casing_short'
    }),
    box('left_drive_post', [0, 3, 3], [2, 13, 13], {
      north: '#gearbox_top',
      east: '#casing',
      south: '#gearbox_top',
      west: '#gearbox',
      up: '#casing_short',
      down: '#casing_short'
    }),
    box('right_drive_post', [14, 3, 3], [16, 13, 13], {
      north: '#gearbox_top',
      east: '#gearbox',
      south: '#gearbox_top',
      west: '#casing',
      up: '#casing_short',
      down: '#casing_short'
    })
  ];

  const machineElements = {
    crystal_pull_furnace: [
      box('heated_copper_body', [1, 3, 1], [15, 9, 15], allFaces('#casing', '#top', '#casing_short')),
      box('front_furnace_mouth', [4, 4, 0], [12, 8, 2], {
        north: '#front',
        east: '#casing',
        west: '#casing',
        up: '#casing_short',
        down: '#casing_short'
      }),
      box('inner_glow_chamber', [5, 4, 2], [11, 8, 3], allFaces('#accent', '#accent', '#accent')),
      box('pulling_tube', [5, 9, 5], [11, 16, 11], allFaces('#pipe', '#detail', '#pipe')),
      box('top_brass_cap', [4, 15, 4], [12, 16, 12], allFaces('#gearbox_top', '#gearbox_top', '#gearbox_top')),
      box('side_drive', [14, 5, 5], [16, 11, 11], allFaces('#gearbox', '#gearbox_top', '#gearbox_top')),
      box('pulling_rod', [7, 9, 7], [9, 16, 9], allFaces('#accent', '#accent', '#accent'))
    ],
    wafer_saw: [
      box('saw_bed', [2, 3, 2], [14, 5, 14], allFaces('#casing', '#top', '#casing_short')),
      box('wafer_channel', [3, 5, 6], [13, 7, 10], allFaces('#front', '#detail', '#casing')),
      box('top_saw_housing', [0, 13, 0], [16, 16, 12], {
        north: '#gearbox',
        east: '#casing_short',
        south: '#drive',
        west: '#casing_short',
        up: '#casing_short',
        down: '#casing'
      }),
      box('thin_cutting_blade', [7, 5, 2], [9, 13, 14], {
        east: ['#detail', [4, 0, 12, 16]],
        west: ['#detail', [4, 0, 12, 16]],
        up: ['#accent', [6, 0, 10, 16]],
        down: ['#accent', [6, 0, 10, 16]]
      }),
      box('front_service_panel', [3, 3, 0], [13, 8, 2], { north: '#front', up: '#casing_short', down: '#casing_short' })
    ],
    wafer_polisher: [
      box('polisher_basin', [1, 3, 1], [15, 10, 15], allFaces('#basin', '#top', '#basin')),
      box('inner_polishing_bowl', [3, 9, 3], [13, 11, 13], allFaces('#detail', '#detail', '#basin')),
      box('central_spindle', [6, 11, 6], [10, 15, 10], allFaces('#gearbox', '#gearbox_top', '#gearbox_top')),
      box('side_reduction_gear', [14, 5, 5], [16, 11, 11], allFaces('#gearbox', '#gearbox_top', '#gearbox_top')),
      box('front_tray_lip', [2, 3, 0], [14, 7, 2], { north: '#front', east: '#casing_short', west: '#casing_short', up: '#accent', down: '#casing' }),
      box('polishing_head', [5, 12, 5], [11, 14, 11], allFaces('#accent', '#detail', '#gearbox_top'))
    ],
    oxidation_diffusion_furnace: [
      box('tube_furnace_body', [2, 4, 2], [14, 12, 14], allFaces('#casing', '#top', '#casing_short')),
      box('diffusion_tube', [4, 6, 0], [12, 10, 16], allFaces('#detail', '#pipe', '#pipe')),
      box('front_heater_mouth', [3, 5, 0], [13, 11, 2], {
        north: '#front',
        east: '#casing',
        west: '#casing',
        up: '#casing_short',
        down: '#casing_short'
      }),
      box('fan_side_drive', [14, 4, 4], [16, 12, 12], allFaces('#fan', '#gearbox_top', '#gearbox_top')),
      box('heater_core', [5, 6, 2], [11, 10, 4], allFaces('#accent', '#accent', '#accent')),
      box('top_copper_rail', [1, 12, 4], [15, 15, 12], allFaces('#casing_short', '#pipe', '#casing'))
    ],
    photolithography_table: [
      box('exposure_table', [2, 3, 2], [14, 6, 14], allFaces('#casing', '#top', '#casing_short')),
      box('wafer_stage', [4, 6, 4], [12, 7, 12], allFaces('#detail', '#detail', '#casing')),
      box('left_optic_column', [3, 6, 3], [5, 13, 5], allFaces('#gearbox_top', '#gearbox_top', '#gearbox_top')),
      box('right_optic_column', [11, 6, 3], [13, 13, 5], allFaces('#gearbox_top', '#gearbox_top', '#gearbox_top')),
      box('exposure_head', [3, 12, 3], [13, 15, 13], allFaces('#press_top', '#front', '#gearbox')),
      box('lens_block', [6, 8, 6], [10, 12, 10], allFaces('#accent', '#detail', '#press_top')),
      box('front_control_panel', [3, 3, 0], [13, 7, 2], { north: '#front', up: '#casing_short', down: '#casing_short' })
    ],
    packaging_test_bench: [
      box('test_bench_top', [1, 3, 1], [15, 6, 15], allFaces('#casing', '#top', '#casing_short')),
      box('ceramic_socket', [5, 6, 5], [11, 8, 11], allFaces('#detail', '#detail', '#press_top')),
      box('front_instrument_panel', [2, 3, 0], [14, 8, 2], {
        north: '#front',
        east: '#casing',
        west: '#casing',
        up: '#casing_short',
        down: '#casing_short'
      }),
      box('left_probe_arm', [3, 8, 4], [5, 14, 12], allFaces('#gearbox_top', '#gearbox_top', '#gearbox_top')),
      box('right_probe_arm', [11, 8, 4], [13, 14, 12], allFaces('#gearbox_top', '#gearbox_top', '#gearbox_top')),
      box('probe_head', [4, 13, 4], [12, 15, 12], allFaces('#press_top', '#accent', '#gearbox')),
      box('test_indicator_row', [4, 8, 2], [12, 9, 4], allFaces('#accent', '#accent', '#accent'))
    ]
  }[name];

  model.elements.push(...base(), ...machineElements);
  return model;
}

function transparentItem(draw) {
  return pixelBuffer((x, y) => draw(x, y));
}

function dust(colors) {
  const points = new Set(['5,5','6,4','7,4','8,5','4,7','5,7','6,6','7,7','8,7','9,6','10,7','5,9','6,9','7,10','8,9','9,9','10,10']);
  return transparentItem((x, y) => {
    if (!points.has(`${x},${y}`)) return [0, 0, 0, 0];
    return withAlpha(colors[(x + y) % colors.length]);
  });
}

function chunkItem(colors) {
  const points = new Set(['6,3','7,3','5,4','6,4','7,4','8,4','4,5','5,5','6,5','7,5','8,5','9,5','5,6','6,6','7,6','8,6','6,7','7,7','8,7','7,8','8,8','9,8']);
  return transparentItem((x, y) => points.has(`${x},${y}`) ? withAlpha(colors[(x * 3 + y) % colors.length]) : [0, 0, 0, 0]);
}

function boule() {
  return transparentItem((x, y) => {
    if (x >= 6 && x <= 9 && y >= 2 && y <= 12) return withAlpha(shade([85, 91, 103], x, y));
    if ((x === 5 || x === 10) && y >= 4 && y <= 10) return withAlpha([50, 55, 64]);
    if (x >= 6 && x <= 9 && y === 1) return withAlpha([160, 170, 188]);
    if (x >= 6 && x <= 9 && y === 13) return withAlpha([38, 41, 48]);
    return [0, 0, 0, 0];
  });
}

function wafer(kind) {
  const palettes = {
    rough_wafer: [[111, 115, 126], [82, 86, 96], [151, 156, 170]],
    polished_wafer: [[190, 200, 212], [126, 143, 158], [228, 235, 240]],
    oxidized_wafer: [[185, 214, 230], [101, 154, 185], [227, 244, 249]],
    photoresist_wafer: [[118, 101, 190], [78, 64, 139], [184, 163, 235]],
    etched_wafer: [[89, 98, 112], [42, 48, 58], [167, 178, 190]],
    doped_wafer: [[104, 156, 129], [59, 95, 77], [190, 220, 200]],
    metallized_wafer: [[203, 177, 101], [117, 97, 58], [241, 221, 146]],
    finished_wafer: [[75, 117, 98], [34, 53, 49], [216, 177, 75]]
  };
  const colors = palettes[kind];
  return transparentItem((x, y) => {
    const dx = x - 8;
    const dy = y - 8;
    const d = dx * dx + dy * dy;
    if (d > 42 || d < 2) return [0, 0, 0, 0];
    let color = colors[(x + y) % colors.length];
    if ((x === 8 || y === 8 || (x + y) % 5 === 0) && d < 38) color = colors[2];
    return withAlpha(color);
  });
}

function die() {
  return transparentItem((x, y) => {
    if (x >= 4 && x <= 11 && y >= 4 && y <= 11) {
      if (x === 4 || x === 11 || y === 4 || y === 11) return withAlpha([211, 171, 63]);
      if ((x + y) % 3 === 0) return withAlpha([62, 154, 98]);
      return withAlpha([37, 92, 73]);
    }
    return [0, 0, 0, 0];
  });
}

function packageItem(chip = false) {
  return transparentItem((x, y) => {
    if (x >= 3 && x <= 12 && y >= 4 && y <= 11) {
      if (x === 3 || x === 12 || y === 4 || y === 11) return withAlpha([105, 91, 72]);
      if (chip && x >= 5 && x <= 10 && y >= 6 && y <= 9) return withAlpha((x + y) % 2 === 0 ? [35, 84, 68] : [206, 162, 63]);
      return withAlpha([220, 209, 182]);
    }
    if ((x === 2 || x === 13) && y >= 5 && y <= 10) return withAlpha([186, 145, 63]);
    return [0, 0, 0, 0];
  });
}

for (const name of machines) {
  writeJson(`src/main/resources/assets/createaddon/blockstates/${name}.json`, {
    variants: { '': { model: `createaddon:block/${name}` } }
  });
  writeJson(`src/main/resources/assets/createaddon/models/block/${name}.json`, createStyleMachineModel(name));
  writeJson(`src/main/resources/assets/createaddon/models/item/${name}.json`, {
    parent: `createaddon:block/${name}`
  });
  writeJson(`src/main/resources/data/createaddon/loot_table/blocks/${name}.json`, {
    type: 'minecraft:block',
    pools: [{
      rolls: 1,
      entries: [{ type: 'minecraft:item', name: `createaddon:${name}` }],
      conditions: [{ condition: 'minecraft:survives_explosion' }]
    }]
  });
  writePng(`src/main/resources/assets/createaddon/textures/block/${name}.png`, machineTexture(name));
  for (const slot of ['top', 'front', 'detail', 'accent']) {
    writePng(`src/main/resources/assets/createaddon/textures/block/${name}_${slot}.png`, refinedMachineTexture(name, slot));
  }
}

for (const name of items) {
  writeJson(`src/main/resources/assets/createaddon/models/item/${name}.json`, {
    parent: 'minecraft:item/generated',
    textures: { layer0: `createaddon:item/${name}` }
  });
}

writePng('src/main/resources/assets/createaddon/textures/item/silica_dust.png', dust([[221, 219, 204], [182, 184, 178], [238, 236, 224]]));
writePng('src/main/resources/assets/createaddon/textures/item/raw_silicon.png', chunkItem([[83, 88, 99], [126, 132, 145], [50, 55, 64]]));
writePng('src/main/resources/assets/createaddon/textures/item/silicon_boule.png', boule());
for (const name of ['rough_wafer', 'polished_wafer', 'oxidized_wafer', 'photoresist_wafer', 'etched_wafer', 'doped_wafer', 'metallized_wafer', 'finished_wafer']) {
  writePng(`src/main/resources/assets/createaddon/textures/item/${name}.png`, wafer(name));
}
writePng('src/main/resources/assets/createaddon/textures/item/bare_logic_die.png', die());
writePng('src/main/resources/assets/createaddon/textures/item/ceramic_chip_package.png', packageItem(false));
writePng('src/main/resources/assets/createaddon/textures/item/basic_logic_chip.png', packageItem(true));

const shaped = (pattern, key, result, count = 1, category = 'misc') => ({
  type: 'minecraft:crafting_shaped',
  category,
  pattern,
  key,
  result: { count, id: result }
});

const item = (id) => ({ item: id });

const machineRecipes = {
  crystal_pull_furnace: shaped(['TET', 'CBC', 'TBT'], {
    T: item('createaddon:tungsten_ingot'),
    E: item('create:electron_tube'),
    C: item('minecraft:copper_ingot'),
    B: item('create:blaze_burner')
  }, 'createaddon:crystal_pull_furnace'),
  wafer_saw: shaped(['TST', 'ACA', 'TPT'], {
    T: item('createaddon:tungsten_ingot'),
    S: item('create:mechanical_saw'),
    A: item('create:andesite_alloy'),
    C: item('create:copper_casing'),
    P: item('create:precision_mechanism')
  }, 'createaddon:wafer_saw'),
  wafer_polisher: shaped(['SPS', 'ACA', 'TBT'], {
    S: item('minecraft:sand'),
    P: item('create:mechanical_press'),
    A: item('create:andesite_alloy'),
    C: item('create:brass_casing'),
    T: item('createaddon:tin_ingot'),
    B: item('create:basin')
  }, 'createaddon:wafer_polisher'),
  oxidation_diffusion_furnace: shaped(['TET', 'CBC', 'TBT'], {
    T: item('createaddon:tungsten_ingot'),
    E: item('create:encased_fan'),
    C: item('minecraft:copper_ingot'),
    B: item('create:blaze_burner')
  }, 'createaddon:oxidation_diffusion_furnace'),
  photolithography_table: shaped(['LGL', 'EPE', 'BCB'], {
    L: item('minecraft:lapis_lazuli'),
    G: item('minecraft:glass'),
    E: item('create:electron_tube'),
    P: item('create:precision_mechanism'),
    B: item('create:brass_sheet'),
    C: item('create:brass_casing')
  }, 'createaddon:photolithography_table'),
  packaging_test_bench: shaped(['TET', 'RPR', 'BCB'], {
    T: item('createaddon:tin_ingot'),
    E: item('create:electron_tube'),
    R: item('minecraft:redstone'),
    P: item('create:precision_mechanism'),
    B: item('minecraft:brick'),
    C: item('create:brass_casing')
  }, 'createaddon:packaging_test_bench')
};

for (const [name, recipe] of Object.entries(machineRecipes)) {
  writeJson(`src/main/resources/data/createaddon/recipe/${name}.json`, recipe);
}

const processingRecipes = {
  silica_dust_from_quartz: {
    type: 'create:crushing',
    ingredients: [item('minecraft:quartz')],
    processing_time: 150,
    results: [{ id: 'createaddon:silica_dust', count: 2 }]
  },
  raw_silicon_from_smelting_silica_dust: {
    type: 'minecraft:smelting',
    category: 'misc',
    cookingtime: 200,
    experience: 0.7,
    group: 'raw_silicon',
    ingredient: item('createaddon:silica_dust'),
    result: { id: 'createaddon:raw_silicon' }
  },
  raw_silicon_from_blasting_silica_dust: {
    type: 'minecraft:blasting',
    category: 'misc',
    cookingtime: 100,
    experience: 0.7,
    group: 'raw_silicon',
    ingredient: item('createaddon:silica_dust'),
    result: { id: 'createaddon:raw_silicon' }
  },
  silicon_boule: shaped([' R ', 'RTR', ' R '], {
    R: item('createaddon:raw_silicon'),
    T: item('createaddon:tungsten_ingot')
  }, 'createaddon:silicon_boule'),
  rough_wafer: shaped([' S ', 'SBS', ' S '], {
    S: item('createaddon:silica_dust'),
    B: item('createaddon:silicon_boule')
  }, 'createaddon:rough_wafer', 4),
  polished_wafer: {
    type: 'create:sandpaper_polishing',
    ingredients: [item('createaddon:rough_wafer')],
    results: [{ id: 'createaddon:polished_wafer' }]
  },
  oxidized_wafer: {
    type: 'minecraft:smelting',
    category: 'misc',
    cookingtime: 200,
    experience: 0.3,
    ingredient: item('createaddon:polished_wafer'),
    result: { id: 'createaddon:oxidized_wafer' }
  },
  photoresist_wafer: shaped([' R ', 'GOG', ' R '], {
    R: item('minecraft:redstone'),
    G: item('minecraft:glowstone_dust'),
    O: item('createaddon:oxidized_wafer')
  }, 'createaddon:photoresist_wafer'),
  etched_wafer: shaped([' Q ', 'RWR', ' Q '], {
    Q: item('minecraft:quartz'),
    R: item('minecraft:redstone'),
    W: item('createaddon:photoresist_wafer')
  }, 'createaddon:etched_wafer'),
  doped_wafer: {
    type: 'minecraft:smelting',
    category: 'misc',
    cookingtime: 200,
    experience: 0.5,
    ingredient: item('createaddon:etched_wafer'),
    result: { id: 'createaddon:doped_wafer' }
  },
  metallized_wafer: shaped(['TTT', 'RWR', 'TTT'], {
    T: item('createaddon:tin_ingot'),
    R: item('minecraft:redstone'),
    W: item('createaddon:doped_wafer')
  }, 'createaddon:metallized_wafer'),
  finished_wafer: shaped([' E ', 'RWR', ' E '], {
    E: item('create:electron_tube'),
    R: item('minecraft:redstone'),
    W: item('createaddon:metallized_wafer')
  }, 'createaddon:finished_wafer'),
  bare_logic_die: shaped([' T ', 'TWT', ' T '], {
    T: item('createaddon:tungsten_ingot'),
    W: item('createaddon:finished_wafer')
  }, 'createaddon:bare_logic_die', 8),
  ceramic_chip_package: shaped(['BBB', 'T T', 'BBB'], {
    B: item('minecraft:brick'),
    T: item('createaddon:tin_ingot')
  }, 'createaddon:ceramic_chip_package', 4),
  basic_logic_chip: shaped([' T ', 'PDP', ' R '], {
    T: item('createaddon:tin_ingot'),
    P: item('createaddon:ceramic_chip_package'),
    D: item('createaddon:bare_logic_die'),
    R: item('minecraft:redstone')
  }, 'createaddon:basic_logic_chip')
};

for (const [name, recipe] of Object.entries(processingRecipes)) {
  writeJson(`src/main/resources/data/createaddon/recipe/${name}.json`, recipe);
}

await import('./refine-wafer-polisher-assets.mjs');

console.log('Generated chip production resources');
