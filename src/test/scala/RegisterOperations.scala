import spatial.dsl._

@spatial class RegisterOperations extends SpatialTest {
  type RegType = FixPt[TRUE, _24, _8]
  type InstructionFixed = FixPt[FALSE, _24, _0]
  type InstBit = FixPt[FALSE, _8, _0]
  type SubType = FixPt[TRUE, _23, _8]

  @struct class Vector3(
    x: RegType,
    y: RegType,
    z: RegType
  )

  // Number of instructions in the file (need a way for this to be dynamic)
  val num_instructions = 29
  val num_vec_elements = 3
  val num_bits = 24
  val pixel_rows = 1
  val pixel_columns = 1
  val registers = 16
  val num_operations = 19
  val square_root_table_elements = 2000 // For square root calculating, up to 4096 but takes a lot longer
  val square_root_table_cols = 2

  def main(args: Array[String]): Unit = {
    val inst_host = loadCSV1D[InstBit](s"$DATA/shader.csv")
    val square_map_host = loadCSV1D[Int](s"$DATA/squares.csv")
    val inst_dram = DRAM[InstBit](num_instructions, num_bits)
    val square_map_dram = DRAM[Int](square_root_table_elements, square_root_table_cols)

    setMem(inst_dram, inst_host)
    setMem(square_map_dram, square_map_host)

    val out = DRAM[RegType](pixel_rows, pixel_columns, num_instructions, num_vec_elements + 1)

    Accel {
      // Create the registers for each pixel
      val vec_regs = SRAM[Vector3](pixel_rows, pixel_columns, registers).buffer
      val sca_regs = SRAM[RegType](pixel_rows, pixel_columns, registers).buffer

      Foreach (0 until pixel_rows) { row =>
        Foreach (0 until pixel_columns) { col =>
          Foreach (0 until registers) { i =>
            vec_regs(row, col, i) = Vector3(0, 0, 0)
            sca_regs(row, col, i) = 0
          }
        }
      }

      val inst_sram = SRAM[InstBit](num_instructions, num_bits)
      inst_sram load inst_dram

      val square_map_sram = SRAM[Int](square_root_table_elements, square_root_table_cols)
      square_map_sram load square_map_dram

      val internal_out = SRAM[RegType](pixel_rows, pixel_columns, num_instructions, num_vec_elements + 1)

      val squares_for_square_roots = SRAM[RegType](square_root_table_elements)

      Foreach (0 until square_root_table_elements) { num =>
        squares_for_square_roots(num) = square_map_sram(num, 1).to[RegType]
      }

      val vec_operations = SRAM[Vector3](pixel_rows, pixel_columns, num_operations)
      val sca_operations = SRAM[RegType](pixel_rows, pixel_columns, num_operations)

      Foreach (0 until pixel_rows) { row =>
        Foreach (0 until pixel_columns) { col =>
          Foreach (0 until num_instructions) { i =>
            val vec_compare_choice = SRAM[Vector3](2)
            val sca_compare_choice = SRAM[RegType](2)

            // Decoding the instruction and storing it as an instruction (for clarity)
            val comp = inst_sram(i, 1) * 8 + inst_sram(i, 2) * 4 + inst_sram(i, 3) * 2 + inst_sram(i, 4)
            val op = inst_sram(i, 5) * 16 + inst_sram(i, 6) * 8 + inst_sram(i, 7) * 4 + inst_sram(i, 8) * 2 + inst_sram(i, 9)
            val src1 = inst_sram(i, 12) * 8 + inst_sram(i, 13) * 4 + inst_sram(i, 14) * 2 + inst_sram(i, 15)
            val src2 = inst_sram(i, 16) * 8 + inst_sram(i, 17) * 4 + inst_sram(i, 18) * 2 + inst_sram(i, 19)
            val dest = inst_sram(i, 20) * 8 + inst_sram(i, 21) * 4 + inst_sram(i, 22) * 2 + inst_sram(i, 23)
            val immediate = inst_sram(i, 10) * 32 + inst_sram(i, 11) * 16 + inst_sram(i, 12) * 8 + inst_sram(i, 13) * 4 + inst_sram(i, 14) * 2 + inst_sram(i, 15)

            val vec_reg_src1 = vec_regs(row, col, src1.to[Int])
            val vec_reg_src2 = vec_regs(row, col, src2.to[Int])
            val sca_reg_src1 = sca_regs(row, col, src1.to[Int])
            val sca_reg_src2 = sca_regs(row, col, src2.to[Int])
            val sca_reg_comp = sca_regs(row, col, comp.to[Int])
            val immediate_regtype = immediate.to[RegType]

            val compare_flag = (comp.to[Int] == 0 || sca_reg_comp.to[Int] == 1).to[Int]

            val add_vectors = Vector3((vec_reg_src1.x.to[SubType] + vec_reg_src2.x.to[SubType]).to[RegType], (vec_reg_src1.y.to[SubType] + vec_reg_src2.y.to[SubType]).to[RegType], (vec_reg_src1.z.to[SubType] + vec_reg_src2.z.to[SubType]).to[RegType])

            val sub_vectors = Vector3((vec_reg_src1.x.to[SubType] - vec_reg_src2.x.to[SubType]).to[RegType], (vec_reg_src1.y.to[SubType] - vec_reg_src2.y.to[SubType]).to[RegType], (vec_reg_src1.z.to[SubType] - vec_reg_src2.z.to[SubType]).to[RegType])

            val src2_mag2 = (vec_reg_src2.x.to[SubType] * vec_reg_src2.x.to[SubType] + vec_reg_src2.y.to[SubType] * vec_reg_src2.y.to[SubType] + vec_reg_src2.z.to[SubType] * vec_reg_src2.z.to[SubType]).to[RegType]

            // Taylor Approximation of Square Root: https://math.libretexts.org/Bookshelves/Analysis/Supplemental_Modules_(Analysis)/Series_and_Expansions/Taylor_Expansion_II
            val ts_for_src2_mag_square = squares_for_square_roots(src2_mag2.to[Int])
            val ts_for_src2_mag_ = (ts_for_src2_mag_square * ts_for_src2_mag_square).to[RegType]
            val ts_for_src2_mag_square_cast = ts_for_src2_mag_square.to[RegType]
            val ts_for_src2_mag_divided = (src2_mag2.to[SubType] / ts_for_src2_mag_.to[SubType]).to[RegType]
            val ts_for_src2_mag_shfted = (ts_for_src2_mag_divided.to[SubType] - 1.0).to[RegType]
            val ts_for_src2_mag_shfted_sqed = (ts_for_src2_mag_shfted.to[SubType] * ts_for_src2_mag_shfted.to[SubType]).to[RegType]
            val ts_for_src2_mag_shfted_tred = (ts_for_src2_mag_shfted.to[SubType] * ts_for_src2_mag_shfted.to[SubType] * ts_for_src2_mag_shfted.to[SubType]).to[RegType]
            val ts_for_src2_mag_sqrt_elem1 = 1.0.to[RegType]
            val ts_for_src2_mag_sqrt_elem2 = (ts_for_src2_mag_shfted.to[SubType] / 2.0).to[RegType]
            val ts_for_src2_mag_sqrt_elem3 = (ts_for_src2_mag_shfted_sqed.to[SubType] / 4.0).to[RegType]
            val ts_for_src2_mag_sqrt_elem4 = (ts_for_src2_mag_shfted_tred.to[SubType] / 16.0).to[RegType]
            val ts_for_src2_mag_sqrt_series = (ts_for_src2_mag_sqrt_elem1.to[SubType] + ts_for_src2_mag_sqrt_elem2.to[SubType] - ts_for_src2_mag_sqrt_elem3.to[SubType] + ts_for_src2_mag_sqrt_elem4.to[SubType]).to[RegType]
            val src2_mag = (ts_for_src2_mag_square_cast.to[SubType] * ts_for_src2_mag_sqrt_series.to[SubType]).to[RegType]

            val normalize_vector = Vector3((vec_reg_src2.x.to[SubType] / (src2_mag.to[SubType])).to[RegType], (vec_reg_src2.y.to[SubType] / src2_mag.to[SubType]).to[RegType], (vec_reg_src2.z.to[SubType] / src2_mag.to[SubType]).to[RegType])

            val dot_product = (vec_reg_src1.x.to[SubType] * vec_reg_src2.x.to[SubType] + vec_reg_src1.y.to[SubType] * vec_reg_src2.y.to[SubType] + vec_reg_src1.z.to[SubType] * vec_reg_src2.z.to[SubType]).to[RegType]

            val mult_vscalar = Vector3((vec_reg_src1.x.to[SubType] * sca_reg_src2.to[SubType]).to[RegType], (vec_reg_src1.y.to[SubType] * sca_reg_src2.to[SubType]).to[RegType], (vec_reg_src1.z.to[SubType] * sca_reg_src2.to[SubType]).to[RegType]) 
            val div_vscalar = Vector3((vec_reg_src1.x.to[SubType] / sca_reg_src2.to[SubType]).to[RegType], (vec_reg_src1.y.to[SubType] / sca_reg_src2.to[SubType]).to[RegType], (vec_reg_src1.z.to[SubType] / sca_reg_src2.to[SubType]).to[RegType])

            val abs_src2 = SRAM[Int](2)
            abs_src2(0) = sca_reg_src2.to[Int]
            abs_src2(1) = (sca_reg_src2.to[SubType] * -1).to[Int]
            val is_src2_negative = (sca_reg_src2 < 1).to[Int]

            val ts_for_src2_sca_square = squares_for_square_roots(abs_src2(is_src2_negative))
            val ts_for_src2_sca_ = (ts_for_src2_sca_square * ts_for_src2_sca_square).to[RegType]
            val ts_for_src2_sca_square_cast = ts_for_src2_sca_square.to[RegType]
            val ts_for_src2_sca_divided = (sca_reg_src2.to[SubType] / ts_for_src2_sca_.to[SubType]).to[RegType]
            val ts_for_src2_sca_shfted = (ts_for_src2_sca_divided.to[SubType] - 1.0).to[RegType]
            val ts_for_src2_sca_shfted_sqed = (ts_for_src2_sca_shfted.to[SubType] * ts_for_src2_sca_shfted.to[SubType]).to[RegType]
            val ts_for_src2_sca_shfted_tred = (ts_for_src2_sca_shfted.to[SubType] * ts_for_src2_sca_shfted.to[SubType] * ts_for_src2_sca_shfted.to[SubType]).to[RegType]
            val ts_for_src2_sca_sqrt_elem1 = 1.0.to[RegType]
            val ts_for_src2_sca_sqrt_elem2 = (ts_for_src2_sca_shfted.to[SubType] / 2.0).to[RegType]
            val ts_for_src2_sca_sqrt_elem3 = (ts_for_src2_sca_shfted_sqed.to[SubType] / 4.0).to[RegType]
            val ts_for_src2_sca_sqrt_elem4 = (ts_for_src2_sca_shfted_tred.to[SubType] / 16.0).to[RegType]
            val ts_for_src2_sca_sqrt_series = (ts_for_src2_sca_sqrt_elem1.to[SubType] + ts_for_src2_sca_sqrt_elem2.to[SubType] - ts_for_src2_sca_sqrt_elem3.to[SubType] + ts_for_src2_sca_sqrt_elem4.to[SubType]).to[RegType]
            val sqrt_scalar = (ts_for_src2_sca_square_cast.to[SubType] * ts_for_src2_sca_sqrt_series.to[SubType]).to[RegType]

            val add_scalar = (sca_reg_src1.to[SubType] + sca_reg_src2.to[SubType]).to[RegType]
            val sub_scalar = (sca_reg_src1.to[SubType] - sca_reg_src2.to[SubType]).to[RegType]
            val mult_scalar = (sca_reg_src1.to[SubType] * sca_reg_src2.to[SubType]).to[RegType]
            val div_scalar = (sca_reg_src1.to[SubType] / sca_reg_src2.to[SubType]).to[RegType]

            val addi_scalar = (sca_reg_src2.to[SubType] + immediate_regtype.to[SubType]).to[RegType]

            val addi_vector_x = Vector3(immediate_regtype + vec_reg_src2.x, vec_reg_src2.y, vec_reg_src2.z)
            val addi_vector_y = Vector3(vec_reg_src2.x, immediate_regtype + vec_reg_src2.y, vec_reg_src2.z)
            val addi_vector_z = Vector3(vec_reg_src2.x, vec_reg_src2.y, immediate_regtype + vec_reg_src2.z)

            val less_than = (sca_reg_src1.to[SubType] < sca_reg_src2.to[SubType]).to[RegType]
            val greater_than_or_equal = (sca_reg_src1.to[SubType] >= sca_reg_src2.to[SubType]).to[RegType]

            vec_operations(row, col, 0) = add_vectors
            vec_operations(row, col, 1) = sub_vectors
            vec_operations(row, col, 2) = normalize_vector
            vec_operations(row, col, 3) = vec_regs(row, col, dest.to[Int])
            vec_operations(row, col, 4) = vec_regs(row, col, dest.to[Int])
            vec_operations(row, col, 5) = vec_regs(row, col, dest.to[Int])
            vec_operations(row, col, 6) = mult_vscalar
            vec_operations(row, col, 7) = div_vscalar
            vec_operations(row, col, 8) = vec_regs(row, col, dest.to[Int])
            vec_operations(row, col, 9) = vec_regs(row, col, dest.to[Int])
            vec_operations(row, col, 10) = vec_regs(row, col, dest.to[Int])
            vec_operations(row, col, 11) = vec_regs(row, col, dest.to[Int])
            vec_operations(row, col, 12) = vec_regs(row, col, dest.to[Int])
            vec_operations(row, col, 13) = vec_regs(row, col, dest.to[Int])
            vec_operations(row, col, 14) = addi_vector_x
            vec_operations(row, col, 15) = addi_vector_y
            vec_operations(row, col, 16) = addi_vector_z
            vec_operations(row, col, 17) = vec_regs(row, col, dest.to[Int])
            vec_operations(row, col, 18) = vec_regs(row, col, dest.to[Int])

            sca_operations(row, col, 0) = sca_regs(row, col, dest.to[Int])
            sca_operations(row, col, 1) = sca_regs(row, col, dest.to[Int])
            sca_operations(row, col, 2) = sca_regs(row, col, dest.to[Int])
            sca_operations(row, col, 3) = src2_mag
            sca_operations(row, col, 4) = src2_mag2
            sca_operations(row, col, 5) = dot_product
            sca_operations(row, col, 6) = sca_regs(row, col, dest.to[Int])
            sca_operations(row, col, 7) = sca_regs(row, col, dest.to[Int])
            sca_operations(row, col, 8) = sqrt_scalar
            sca_operations(row, col, 9) = add_scalar
            sca_operations(row, col, 10) = sub_scalar
            sca_operations(row, col, 11) = mult_scalar
            sca_operations(row, col, 12) = div_scalar
            sca_operations(row, col, 13) = addi_scalar
            sca_operations(row, col, 14) = sca_regs(row, col, dest.to[Int])
            sca_operations(row, col, 15) = sca_regs(row, col, dest.to[Int])
            sca_operations(row, col, 16) = sca_regs(row, col, dest.to[Int])
            sca_operations(row, col, 17) = less_than
            sca_operations(row, col, 18) = greater_than_or_equal

            vec_compare_choice(0) = vec_regs(row, col, dest.to[Int])
            vec_compare_choice(1) = vec_operations(row, col, op.to[Int])

            sca_compare_choice(0) = sca_regs(row, col, dest.to[Int])
            sca_compare_choice(1) = sca_operations(row, col, op.to[Int])

            vec_regs(row, col, dest.to[Int]) = vec_compare_choice(compare_flag)
            sca_regs(row, col, dest.to[Int]) = sca_compare_choice(compare_flag)

            internal_out(row, col, i, 0) = vec_regs(row, col, 1).x
            internal_out(row, col, i, 1) = vec_regs(row, col, 1).y
            internal_out(row, col, i, 2) = vec_regs(row, col, 1).z
            internal_out(row, col, i, 3) = sca_regs(row, col, 1)
          }
        }
      }

      out store internal_out
    }

    val result = getMem(out)

    // temporary for basic asm tests
    print("STARTING DATA")
    printArray(result)
    print("ENDING DATA")
    assert(1 == 1)
  }
}