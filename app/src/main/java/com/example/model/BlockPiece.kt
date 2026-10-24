package com.example.model

import java.util.UUID
import kotlin.random.Random

data class BlockPiece(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val matrix: List<List<Boolean>>,
    val colorHex: Long,
    val highlightHex: Long
) {
    val height: Int get() = matrix.size
    val width: Int get() = if (matrix.isNotEmpty()) matrix[0].size else 0
    val blockCount: Int get() = matrix.sumOf { row -> row.count { it } }

    /**
     * Returns true if the cell at relative row r and col c is solid.
     */
    fun isOccupied(r: Int, c: Int): Boolean {
        return r in matrix.indices && c in matrix[r].indices && matrix[r][c]
    }
}

object BlockShapeFactory {

    // Color definitions as hex Longs
    private const val CYAN = 0xFF38BDF8
    private const val CYAN_HL = 0xFFBAE6FD

    private const val ORANGE = 0xFFFB923C
    private const val ORANGE_HL = 0xFFFED7AA

    private const val GREEN = 0xFF4ADE80
    private const val GREEN_HL = 0xFFBBF7D0

    private const val PURPLE = 0xFFC084FC
    private const val PURPLE_HL = 0xFFF3E8FF

    private const val PINK = 0xFFF472B6
    private const val PINK_HL = 0xFFFCE7F3

    private const val YELLOW = 0xFFFACC15
    private const val YELLOW_HL = 0xFFFEF08A

    private const val TEAL = 0xFF2DD4BF
    private const val TEAL_HL = 0xFFCCFBF1

    private const val INDIGO = 0xFF818CF8
    private const val INDIGO_HL = 0xFFE0E7FF

    private const val CORAL = 0xFFF87171
    private const val CORAL_HL = 0xFFFECACA

    private val SHAPE_TEMPLATES = listOf(
        // 1. Single Dot (1x1)
        ShapeTemplate("Dot1x1", listOf(listOf(true)), CYAN, CYAN_HL),

        // 2. 2-Line Horizontal & Vertical
        ShapeTemplate("Line2H", listOf(listOf(true, true)), GREEN, GREEN_HL),
        ShapeTemplate("Line2V", listOf(listOf(true), listOf(true)), GREEN, GREEN_HL),

        // 3. 3-Line Horizontal & Vertical
        ShapeTemplate("Line3H", listOf(listOf(true, true, true)), TEAL, TEAL_HL),
        ShapeTemplate("Line3V", listOf(listOf(true), listOf(true), listOf(true)), TEAL, TEAL_HL),

        // 4. 4-Line Horizontal & Vertical
        ShapeTemplate("Line4H", listOf(listOf(true, true, true, true)), INDIGO, INDIGO_HL),
        ShapeTemplate("Line4V", listOf(listOf(true), listOf(true), listOf(true), listOf(true)), INDIGO, INDIGO_HL),

        // 5. 5-Line Horizontal & Vertical
        ShapeTemplate("Line5H", listOf(listOf(true, true, true, true, true)), CYAN, CYAN_HL),
        ShapeTemplate("Line5V", listOf(listOf(true), listOf(true), listOf(true), listOf(true), listOf(true)), CYAN, CYAN_HL),

        // 6. 2x2 Square
        ShapeTemplate(
            "Square2x2",
            listOf(
                listOf(true, true),
                listOf(true, true)
            ),
            YELLOW, YELLOW_HL
        ),

        // 7. 3x3 Square
        ShapeTemplate(
            "Square3x3",
            listOf(
                listOf(true, true, true),
                listOf(true, true, true),
                listOf(true, true, true)
            ),
            CORAL, CORAL_HL
        ),

        // 8. L-Shapes 2x2 (Corner polyominoes)
        ShapeTemplate("Corner2x2_1", listOf(listOf(true, false), listOf(true, true)), ORANGE, ORANGE_HL),
        ShapeTemplate("Corner2x2_2", listOf(listOf(false, true), listOf(true, true)), ORANGE, ORANGE_HL),
        ShapeTemplate("Corner2x2_3", listOf(listOf(true, true), listOf(true, false)), ORANGE, ORANGE_HL),
        ShapeTemplate("Corner2x2_4", listOf(listOf(true, true), listOf(false, true)), ORANGE, ORANGE_HL),

        // 9. L-Shapes 3x3 (Big L)
        ShapeTemplate(
            "L3x3_1",
            listOf(
                listOf(true, false, false),
                listOf(true, false, false),
                listOf(true, true, true)
            ),
            PURPLE, PURPLE_HL
        ),
        ShapeTemplate(
            "L3x3_2",
            listOf(
                listOf(false, false, true),
                listOf(false, false, true),
                listOf(true, true, true)
            ),
            PURPLE, PURPLE_HL
        ),
        ShapeTemplate(
            "L3x3_3",
            listOf(
                listOf(true, true, true),
                listOf(true, false, false),
                listOf(true, false, false)
            ),
            PURPLE, PURPLE_HL
        ),
        ShapeTemplate(
            "L3x3_4",
            listOf(
                listOf(true, true, true),
                listOf(false, false, true),
                listOf(false, false, true)
            ),
            PURPLE, PURPLE_HL
        ),

        // 10. T-Shapes (3x2)
        ShapeTemplate(
            "T3x2_Down",
            listOf(
                listOf(true, true, true),
                listOf(false, true, false)
            ),
            PINK, PINK_HL
        ),
        ShapeTemplate(
            "T3x2_Up",
            listOf(
                listOf(false, true, false),
                listOf(true, true, true)
            ),
            PINK, PINK_HL
        ),
        ShapeTemplate(
            "T2x3_Right",
            listOf(
                listOf(true, false),
                listOf(true, true),
                listOf(true, false)
            ),
            PINK, PINK_HL
        ),
        ShapeTemplate(
            "T2x3_Left",
            listOf(
                listOf(false, true),
                listOf(true, true),
                listOf(false, true)
            ),
            PINK, PINK_HL
        ),

        // 11. Z and S Shapes
        ShapeTemplate(
            "ZShapeH",
            listOf(
                listOf(true, true, false),
                listOf(false, true, true)
            ),
            GREEN, GREEN_HL
        ),
        ShapeTemplate(
            "SShapeH",
            listOf(
                listOf(false, true, true),
                listOf(true, true, false)
            ),
            GREEN, GREEN_HL
        ),
        ShapeTemplate(
            "ZShapeV",
            listOf(
                listOf(true, false),
                listOf(true, true),
                listOf(false, true)
            ),
            TEAL, TEAL_HL
        ),
        ShapeTemplate(
            "SShapeV",
            listOf(
                listOf(false, true),
                listOf(true, true),
                listOf(true, false)
            ),
            TEAL, TEAL_HL
        ),

        // 12. 2x3 and 3x2 Rectangles
        ShapeTemplate(
            "Rect2x3",
            listOf(
                listOf(true, true, true),
                listOf(true, true, true)
            ),
            CORAL, CORAL_HL
        ),
        ShapeTemplate(
            "Rect3x2",
            listOf(
                listOf(true, true),
                listOf(true, true),
                listOf(true, true)
            ),
            CORAL, CORAL_HL
        )
    )

    private data class ShapeTemplate(
        val name: String,
        val matrix: List<List<Boolean>>,
        val colorHex: Long,
        val highlightHex: Long
    )

    /**
     * Generates a balanced set of 3 random pieces for a round.
     * Prevents giving too many oversized pieces at once so the game remains strategic and fun.
     */
    fun generateRoundPieces(): List<BlockPiece> {
        val selected = mutableListOf<BlockPiece>()
        var hasBigShape = false

        while (selected.size < 3) {
            val template = SHAPE_TEMPLATES[Random.nextInt(SHAPE_TEMPLATES.size)]
            val isBig = template.name == "Square3x3" || template.name.startsWith("Line5") || template.name.startsWith("Rect")
            if (isBig && hasBigShape && Random.nextFloat() > 0.2f) {
                // Skip adding a second huge piece in the same batch most of the time
                continue
            }
            if (isBig) hasBigShape = true

            selected.add(
                BlockPiece(
                    id = UUID.randomUUID().toString(),
                    name = template.name,
                    matrix = template.matrix,
                    colorHex = template.colorHex,
                    highlightHex = template.highlightHex
                )
            )
        }
        return selected
    }
}
