import spatial.dsl._

@spatial class RayTracer extends SpatialTest {
  type RegType = FixPt[FALSE, _16, _0]

  @struct class Instruction(
    agg: FixPt[FALSE, _3, _0],
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

      // Go through each instruction (pipeline this later)
      Foreach (0 until N) { i =>
        val current_inst = inst_sram(i)
        val agg = current_inst.agg
        val oo = current_inst.oo
        val src1 = current_inst.src1
        val src2 = current_inst.src2
        val dest = current_inst.dest

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

        // add
        if (agg == 0 && oo == 0) {
          
        }
        
      }
    }

  }
}