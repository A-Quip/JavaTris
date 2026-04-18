package com.aquip.tetris.piece;

import java.util.EnumMap;
import java.util.Map;

public class PieceRegistry {

    private final Map<PieceType, PieceData> registry = new EnumMap<>(PieceType.class);

    public PieceRegistry() {
        init();
    }

    private static final PieceRegistry INSTANCE = new PieceRegistry();

    public static PieceRegistry getInstance() {
        return INSTANCE;
    }

    private void init() {
        KickTable jltsz = new KickTable(createJLSTZ());
        KickTable iTable = new KickTable(createI());
        KickTable oTable = new KickTable(createO());

        registry.put(PieceType.T, new PieceData(tRotations(), jltsz));
        registry.put(PieceType.J, new PieceData(jRotations(), jltsz));
        registry.put(PieceType.L, new PieceData(lRotations(), jltsz));
        registry.put(PieceType.S, new PieceData(sRotations(), jltsz));
        registry.put(PieceType.Z, new PieceData(zRotations(), jltsz));

        registry.put(PieceType.I, new PieceData(iRotations(), iTable));
        registry.put(PieceType.O, new PieceData(oRotations(), oTable));
    }

    public PieceData get(PieceType type) {
        return registry.get(type);
    }

    public int[][] getRotation(PieceType type, int rotation) {
        int[][][] rotations = get(type).rotations;
        return rotations[rotation % rotations.length];
    }

    public int[][] getKicks(PieceType type, int from, int to) {
        return get(type).getKicks(from & 3, to & 3);
    }

    /**
     * Converts rotation matrix → block positions (local coords)
     */
    public java.util.List<Piece.Position> getBlocks(PieceType type, int rotation) {
        int[][] shape = getRotation(type, rotation);

        java.util.List<Piece.Position> blocks = new java.util.ArrayList<>();

        for (int y = 0; y < shape.length; y++) {
            for (int x = 0; x < shape[y].length; x++) {
                if (shape[y][x] != 0) {
                    blocks.add(new Piece.Position(x, y));
                }
            }
        }

        return blocks;
    }

    /**
     * Converts a piece rotation into an array of bitmasks (one per row).
     * Used for fast bitboard placement.
     */
    public int[] getRotationMasks(PieceType type, int rotation) {
        int[][] shape = getRotation(type, rotation);
        int[] masks = new int[shape.length];
        for (int y = 0; y < shape.length; y++) {
            int mask = 0;
            for (int x = 0; x < shape[y].length; x++) {
                if (shape[y][x] != 0) {
                    mask |= (1 << x);
                }
            }
            masks[y] = mask;
        }
        return masks;
    }

    // =========================
    // ROTATIONS (unchanged)
    // =========================
    // (keep all your rotation methods exactly as-is)

    private int[][][] iRotations() {
        return new int[][][] {
                {
                        {0,0,0,0},
                        {1,1,1,1},
                        {0,0,0,0},
                        {0,0,0,0}
                },
                {
                        {0,0,1,0},
                        {0,0,1,0},
                        {0,0,1,0},
                        {0,0,1,0}
                },
                {
                        {0,0,0,0},
                        {0,0,0,0},
                        {1,1,1,1},
                        {0,0,0,0}
                },
                {
                        {0,1,0,0},
                        {0,1,0,0},
                        {0,1,0,0},
                        {0,1,0,0}
                }
        };
    }
    private int[][][] oRotations() {
        return new int[][][] {
                {
                        {1,1},
                        {1,1}
                }
        };
    }
    private int[][][] tRotations() {
        return new int[][][] {
                {
                        {0,1,0},
                        {1,1,1},
                        {0,0,0}
                },
                {
                        {0,1,0},
                        {0,1,1},
                        {0,1,0}
                },
                {
                        {0,0,0},
                        {1,1,1},
                        {0,1,0}
                },
                {
                        {0,1,0},
                        {1,1,0},
                        {0,1,0}
                }
        };
    }
    private int[][][] sRotations() {
        return new int[][][] {
                {
                        {0,1,1},
                        {1,1,0},
                        {0,0,0}
                },
                {
                        {0,1,0},
                        {0,1,1},
                        {0,0,1}
                },
                {
                        {0,0,0},
                        {0,1,1},
                        {1,1,0}
                },
                {
                        {1,0,0},
                        {1,1,0},
                        {0,1,0}
                }
        };
    }
    private int[][][] zRotations() {
        return new int[][][] {
                {
                        {1,1,0},
                        {0,1,1},
                        {0,0,0}
                },
                {
                        {0,0,1},
                        {0,1,1},
                        {0,1,0}
                },
                {
                        {0,0,0},
                        {1,1,0},
                        {0,1,1}
                },
                {
                        {0,1,0},
                        {1,1,0},
                        {1,0,0}
                }
        };
    }
    private int[][][] jRotations() {
        return new int[][][] {
                {
                        {1,0,0},
                        {1,1,1},
                        {0,0,0}
                },
                {
                        {0,1,1},
                        {0,1,0},
                        {0,1,0}
                },
                {
                        {0,0,0},
                        {1,1,1},
                        {0,0,1}
                },
                {
                        {0,1,0},
                        {0,1,0},
                        {1,1,0}
                }
        };
    }
    private int[][][] lRotations() {
        return new int[][][] {
                {
                        {0,0,1},
                        {1,1,1},
                        {0,0,0}
                },
                {
                        {0,1,0},
                        {0,1,0},
                        {0,1,1}
                },
                {
                        {0,0,0},
                        {1,1,1},
                        {1,0,0}
                },
                {
                        {1,1,0},
                        {0,1,0},
                        {0,1,0}
                }
        };
    }

    // =========================
    // KICK TABLES (unchanged)
    // =========================
    private int[][][][] createJLSTZ() {
        // SRS JLSTZ kicks: from rotation (rows) → to rotation (cols) → [dx, dy] pairs
        // Format: kicks[fromRotation][toRotation][i] = {dx, dy}
        int[][][][] kicks = new int[4][4][5][2];

        // Default: fill all empty
        for (int from = 0; from < 4; from++)
            for (int to = 0; to < 4; to++)
                for (int i = 0; i < 5; i++)
                    kicks[from][to][i] = new int[]{0,0};

        // 0→1
        kicks[0][1] = new int[][]{{0,0},{-1,0},{-1,1},{0,-2},{-1,-2}};
        // 1→0
        kicks[1][0] = new int[][]{{0,0},{1,0},{1,-1},{0,2},{1,2}};
        // 1→2
        kicks[1][2] = new int[][]{{0,0},{1,0},{1,-1},{0,2},{1,2}};
        // 2→1
        kicks[2][1] = new int[][]{{0,0},{-1,0},{-1,1},{0,-2},{-1,-2}};
        // 2→3
        kicks[2][3] = new int[][]{{0,0},{1,0},{1,1},{0,-2},{1,-2}};
        // 3→2
        kicks[3][2] = new int[][]{{0,0},{-1,0},{-1,-1},{0,2},{-1,2}};
        // 3→0
        kicks[3][0] = new int[][]{{0,0},{-1,0},{-1,-1},{0,2},{-1,2}};
        // 0→3
        kicks[0][3] = new int[][]{{0,0},{1,0},{1,1},{0,-2},{1,-2}};

        return kicks;
    }
    private int[][][][] createI() {
        int[][][][] kicks = new int[4][4][5][2];

        // Initialize empty
        for (int from = 0; from < 4; from++)
            for (int to = 0; to < 4; to++)
                for (int i = 0; i < 5; i++)
                    kicks[from][to][i] = new int[]{0,0};

        // 0→1
        kicks[0][1] = new int[][]{{0,0},{-2,0},{1,0},{-2,-1},{1,2}};
        // 1→0
        kicks[1][0] = new int[][]{{0,0},{2,0},{-1,0},{2,1},{-1,-2}};
        // 1→2
        kicks[1][2] = new int[][]{{0,0},{-1,0},{2,0},{-1,2},{2,-1}};
        // 2→1
        kicks[2][1] = new int[][]{{0,0},{1,0},{-2,0},{1,-2},{-2,1}};
        // 2→3
        kicks[2][3] = new int[][]{{0,0},{2,0},{-1,0},{2,1},{-1,-2}};
        // 3→2
        kicks[3][2] = new int[][]{{0,0},{-2,0},{1,0},{-2,-1},{1,2}};
        // 3→0
        kicks[3][0] = new int[][]{{0,0},{1,0},{-2,0},{1,-2},{-2,1}};
        // 0→3
        kicks[0][3] = new int[][]{{0,0},{-1,0},{2,0},{-1,2},{2,-1}};

        return kicks;
    }
    private int[][][][] createO() {
        int[][][][] kicks = new int[4][4][1][2];

        for (int from = 0; from < 4; from++) {
            for (int to = 0; to < 4; to++) {
                kicks[from][to][0] = new int[]{0, 0};
            }
        }

        return kicks;
    }
}