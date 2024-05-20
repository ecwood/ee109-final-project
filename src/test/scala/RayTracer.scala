import spatial.dsl._

@spatial class RayTracer extends SpatialTest {
  type RegType = FixPt[FALSE, _16, _0]

  @struct class Instruction(
    a: FixPt[FALSE, _1, _0],
    gg: FixPt[FALSE, _2, _0],
    oo: FixPt[FALSE, _2, _0],
    src1: FixPt[FALSE, _4, _0],
    src2: FixPt[FALSE, _4, _0],
    dest: FixPt[FALSE, _4, _0]
  )

  @struct class Vector3(
    elem1: RegType,
    elem2: RegType,
    elem3: RegType
  )

  // Number of instructions in the file (need a way for this to be dynamic)
  val N = 1024

  def main(args: Array[String]): Unit = {
    val inst_host = loadCSV1D[Instruction](s"$DATA/instructions.csv")
    val inst_dram = DRAM[Instruction](N)

    setMem(inst_dram, inst_host)

    Accel {
      val inst_sram = SRAM[Instruction](N)

      inst_sram load inst_dram

      // Define all of the registers
      val vec_reg0 = Reg[Vector3]
      val vec_reg1 = Reg[Vector3]
      val vec_reg2 = Reg[Vector3]
      val vec_reg3 = Reg[Vector3]
      val vec_reg4 = Reg[Vector3]
      val vec_reg5 = Reg[Vector3]
      val vec_reg6 = Reg[Vector3]
      val vec_reg7 = Reg[Vector3]
      val vec_reg8 = Reg[Vector3]
      val vec_reg9 = Reg[Vector3]
      val vec_reg10 = Reg[Vector3]
      val vec_reg11 = Reg[Vector3]
      val vec_reg12 = Reg[Vector3]
      val vec_reg13 = Reg[Vector3]
      val vec_reg14 = Reg[Vector3]
      val vec_reg15 = Reg[Vector3]

      val sca_reg0 = Reg[RegType]
      val sca_reg1 = Reg[RegType]
      val sca_reg2 = Reg[RegType]
      val sca_reg3 = Reg[RegType]
      val sca_reg4 = Reg[RegType]
      val sca_reg5 = Reg[RegType]
      val sca_reg6 = Reg[RegType]
      val sca_reg7 = Reg[RegType]
      val sca_reg8 = Reg[RegType]
      val sca_reg9 = Reg[RegType]
      val sca_reg10 = Reg[RegType]
      val sca_reg11 = Reg[RegType]
      val sca_reg12 = Reg[RegType]
      val sca_reg13 = Reg[RegType]
      val sca_reg14 = Reg[RegType]
      val sca_reg15 = Reg[RegType]

      // Create registers for working with the values after they've been decoded
      val vec_src1_val = Reg[Vector3]
      val sca_src1_val = Reg[RegType]
      val vec_src2_val = Reg[Vector3]
      val sca_src2_val = Reg[RegType]

      // Also create registers for saving current destination register values (so that you can store both without
      // checking which one should be stored)
      val vec_dest_val = Reg[Vector3]
      val sca_dest_val = Reg[RegType]

      // Go through each instruction (pipeline this later)
      Foreach (0 until N) { i =>
        Sequential {
          val current_inst = inst_sram(i)
          val a = current_inst.a
          val gg = current_inst.gg
          val oo = current_inst.oo
          val src1 = current_inst.src1
          val src2 = current_inst.src2
          val dest = current_inst.dest
          val imm = src1 + 16 * oo // need all 6 digits, have to shift the oo by 4; TODO: sign extend

          // Decode (into both scalar and vector, just in case)
          // Source 1
          if (src1 == 0) {
            vec_src1_val = vec_reg0
            sca_src1_val = sca_reg0
          }
          if (src1 == 1) {
            vec_src1_val = vec_reg1
            sca_src1_val = sca_reg1
          }
          if (src1 == 2) {
            vec_src1_val = vec_reg2
            sca_src1_val = sca_reg2
          }
          if (src1 == 3) {
            vec_src1_val = vec_reg3
            sca_src1_val = sca_reg3
          }
          if (src1 == 4) {
            vec_src1_val = vec_reg4
            sca_src1_val = sca_reg4
          }
          if (src1 == 5) {
            vec_src1_val = vec_reg5
            sca_src1_val = sca_reg5
          }
          if (src1 == 6) {
            vec_src1_val = vec_reg6
            sca_src1_val = sca_reg6
          }
          if (src1 == 7) {
            vec_src1_val = vec_reg7
            sca_src1_val = sca_reg7
          }
          if (src1 == 8) {
            vec_src1_val = vec_reg8
            sca_src1_val = sca_reg8
          }
          if (src1 == 9) {
            vec_src1_val = vec_reg9
            sca_src1_val = sca_reg9
          }
          if (src1 == 10) {
            vec_src1_val = vec_reg10
            sca_src1_val = sca_reg10
          }
          if (src1 == 11) {
            vec_src1_val = vec_reg11
            sca_src1_val = sca_reg11
          }
          if (src1 == 12) {
            vec_src1_val = vec_reg12
            sca_src1_val = sca_reg12
          }
          if (src1 == 13) {
            vec_src1_val = vec_reg13
            sca_src1_val = sca_reg13
          }
          if (src1 == 14) {
            vec_src1_val = vec_reg14
            sca_src1_val = sca_reg14
          }
          if (src1 == 15) {
            vec_src1_val = vec_reg15
            sca_src1_val = sca_reg15
          }

          // Source 2
          if (src2 == 0) {
            vec_src2_val = vec_reg0
            sca_src2_val = sca_reg0
          }
          if (src2 == 1) {
            vec_src2_val = vec_reg1
            sca_src2_val = sca_reg1
          }
          if (src2 == 2) {
            vec_src2_val = vec_reg2
            sca_src2_val = sca_reg2
          }
          if (src2 == 3) {
            vec_src2_val = vec_reg3
            sca_src2_val = sca_reg3
          }
          if (src2 == 4) {
            vec_src2_val = vec_reg4
            sca_src2_val = sca_reg4
          }
          if (src2 == 5) {
            vec_src2_val = vec_reg5
            sca_src2_val = sca_reg5
          }
          if (src2 == 6) {
            vec_src2_val = vec_reg6
            sca_src2_val = sca_reg6
          }
          if (src2 == 7) {
            vec_src2_val = vec_reg7
            sca_src2_val = sca_reg7
          }
          if (src2 == 8) {
            vec_src2_val = vec_reg8
            sca_src2_val = sca_reg8
          }
          if (src2 == 9) {
            vec_src2_val = vec_reg9
            sca_src2_val = sca_reg9
          }
          if (src2 == 10) {
            vec_src2_val = vec_reg10
            sca_src2_val = sca_reg10
          }
          if (src2 == 11) {
            vec_src2_val = vec_reg11
            sca_src2_val = sca_reg11
          }
          if (src2 == 12) {
            vec_src2_val = vec_reg12
            sca_src2_val = sca_reg12
          }
          if (src2 == 13) {
            vec_src2_val = vec_reg13
            sca_src2_val = sca_reg13
          }
          if (src2 == 14) {
            vec_src2_val = vec_reg14
            sca_src2_val = sca_reg14
          }
          if (src2 == 15) {
            vec_src2_val = vec_reg15
            sca_src2_val = sca_reg15
          }

          // Destination
          if (dest == 0) {
            vec_dest_val = vec_reg0
            sca_dest_val = sca_reg0
          }
          if (dest == 1) {
            vec_dest_val = vec_reg1
            sca_dest_val = sca_reg1
          }
          if (dest == 2) {
            vec_dest_val = vec_reg2
            sca_dest_val = sca_reg2
          }
          if (dest == 3) {
            vec_dest_val = vec_reg3
            sca_dest_val = sca_reg3
          }
          if (dest == 4) {
            vec_dest_val = vec_reg4
            sca_dest_val = sca_reg4
          }
          if (dest == 5) {
            vec_dest_val = vec_reg5
            sca_dest_val = sca_reg5
          }
          if (dest == 6) {
            vec_dest_val = vec_reg6
            sca_dest_val = sca_reg6
          }
          if (dest == 7) {
            vec_dest_val = vec_reg7
            sca_dest_val = sca_reg7
          }
          if (dest == 8) {
            vec_dest_val = vec_reg8
            sca_dest_val = sca_reg8
          }
          if (dest == 9) {
            vec_dest_val = vec_reg9
            sca_dest_val = sca_reg9
          }
          if (dest == 10) {
            vec_dest_val = vec_reg10
            sca_dest_val = sca_reg10
          }
          if (dest == 11) {
            vec_dest_val = vec_reg11
            sca_dest_val = sca_reg11
          }
          if (dest == 12) {
            vec_dest_val = vec_reg12
            sca_dest_val = sca_reg12
          }
          if (dest == 13) {
            vec_dest_val = vec_reg13
            sca_dest_val = sca_reg13
          }
          if (dest == 14) {
            vec_dest_val = vec_reg14
            sca_dest_val = sca_reg14
          }
          if (dest == 15) {
            vec_dest_val = vec_reg15
            sca_dest_val = sca_reg15
          }

          // add
          if (a == 0 && gg == 0 && oo == 0) {
            vec_dest_val.elem1 = vec_src1_val.elem1 + vec_src2_val.elem1
            vec_dest_val.elem2 = vec_src1_val.elem2 + vec_src2_val.elem2
            vec_dest_val.elem3 = vec_src1_val.elem3 + vec_src2_val.elem3
          }

          // subtract
          if (a == 0 && gg == 0 && oo == 1) {
            vec_dest_val.elem1 = vec_src1_val.elem1 - vec_src2_val.elem1
            vec_dest_val.elem2 = vec_src1_val.elem2 - vec_src2_val.elem2
            vec_dest_val.elem3 = vec_src1_val.elem3 - vec_src2_val.elem3
          }

          // normalize
          if (a == 0 && gg == 0 && oo == 2) {
            val magnitude = sqrt(vec_src1_val.elem1 * vec_src1_val.elem1 + vec_src1_val.elem2 * vec_src1_val.elem2 + vec_src1_val.elem3 * vec_src1_val.elem3)
            vec_dest_val.elem1 = vec_src1_val.elem1 / magnitude
            vec_dest_val.elem2 = vec_src1_val.elem2 / magnitude
            vec_dest_val.elem3 = vec_src1_val.elem3 / magnitude
          }

          // magnitude
          if (a == 0 && gg == 1 && oo == 0) {
            sca_dest_val = sqrt(vec_src1_val.elem1 * vec_src1_val.elem1 + vec_src1_val.elem2 * vec_src1_val.elem2 + vec_src1_val.elem3 * vec_src1_val.elem3)
          }

          // 
          if (a == 0 && gg == 1 && oo == 1) {
            sca_dest_val = vec_src1_val.elem1 * vec_src1_val.elem1 + vec_src1_val.elem2 * vec_src1_val.elem2 + vec_src1_val.elem3 * vec_src1_val.elem3
          }

          // dot product
          if (a == 0 && gg == 1 && oo == 2) {
            sca_dest_val = vec_src1_val.elem1 * vec_src2_val.elem1 + vec_src1_val.elem2 * vec_src2_val.elem2 + vec_src1_val.elem3 * vec_src2_val.elem3
          }

          // scalar multiply
          if (a == 0 && gg == 2 && oo == 0) {
            vec_dest_val.elem1 = vec_src1_val.elem1 * sca_src2_val
            vec_dest_val.elem2 = vec_src1_val.elem2 * sca_src2_val
            vec_dest_val.elem3 = vec_src1_val.elem3 * sca_src2_val
          }

          // scalar divide
          if (a == 0 && gg == 2 && oo == 1) {
            vec_dest_val.elem1 = vec_src1_val.elem1 / sca_src2_val
            vec_dest_val.elem2 = vec_src1_val.elem2 / sca_src2_val
            vec_dest_val.elem3 = vec_src1_val.elem3 / sca_src2_val
          }

          // sqrt
          if (a == 0 && gg == 3 && oo == 0) {
            sca_dest_val = sqrt(sca_src2_val)
          }

          if (a == 1) {
            // addi
            if (gg == 0) {
              sca_dest_val = sca_src2_val + imm
            }
            if (gg == 1) {
              vec_dest_val.elem1 = sca_src2_val + imm
            }
            if (gg == 2) {
              vec_dest_val.elem2 = sca_src2_val + imm
            }
            if (gg == 3) {
              vec_dest_val.elem2 = sca_src2_val + imm
            }
          }

          // Store the results
        if (dest == 0) {
          vec_reg0 = vec_dest_val
          sca_reg0 = sca_dest_val
        }
        if (dest == 1) {
          vec_reg1 = vec_dest_val
          sca_reg1 = sca_dest_val
        }
        if (dest == 2) {
          vec_reg2 = vec_dest_val
          sca_reg2 = sca_dest_val
        }
        if (dest == 3) {
          vec_reg3 = vec_dest_val
          sca_reg3 = sca_dest_val
        }
        if (dest == 4) {
          vec_reg4 = vec_dest_val
          sca_reg4 = sca_dest_val
        }
        if (dest == 5) {
          vec_reg5 = vec_dest_val
          sca_reg5 = sca_dest_val
        }
        if (dest == 6) {
          vec_reg6 = vec_dest_val
          sca_reg6 = sca_dest_val
        }
        if (dest == 7) {
          vec_reg7 = vec_dest_val
          sca_reg7 = sca_dest_val
        }
        if (dest == 8) {
          vec_reg8 = vec_dest_val
          sca_reg8 = sca_dest_val
        }
        if (dest == 9) {
          vec_reg9 = vec_dest_val
          sca_reg9 = sca_dest_val
        }
        if (dest == 10) {
          vec_reg10 = vec_dest_val
          sca_reg10 = sca_dest_val
        }
        if (dest == 11) {
          vec_reg11 = vec_dest_val
          sca_reg11 = sca_dest_val
        }
        if (dest == 12) {
          vec_reg12 = vec_dest_val
          sca_reg12 = sca_dest_val
        }
        if (dest == 13) {
          vec_reg13 = vec_dest_val
          sca_reg13 = sca_dest_val
        }
        if (dest == 14) {
          vec_reg14 = vec_dest_val
          sca_reg14 = sca_dest_val
        }
        if (dest == 15) {
          vec_reg15 = vec_dest_val
          sca_reg15 = sca_dest_val
        }
        }
      }
    }

  }
}