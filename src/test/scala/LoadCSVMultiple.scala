import spatial.dsl._

@spatial class LoadCSVMultiple extends SpatialTest {
  type RegType = FixPt[FALSE, _16, _0]
  type InstructionFixed = FixPt[FALSE, _24, _0]
  type InstBit = FixPt[FALSE, _8, _0]

  type OneBitType = FixPt[FALSE, _1, _0]
  type TwoBitsType = FixPt[FALSE, _2, _0]
  type RegLabelType = FixPt[FALSE, _4, _0]

  @struct class Instruction(
    nothing: FixPt[FALSE, _7, _0],
    a: OneBitType,
    gg: TwoBitsType,
    oo: TwoBitsType,
    src1: RegLabelType,
    src2: RegLabelType,
    dest: RegLabelType
  )

  @struct class Vector3(
    elem1: RegType,
    elem2: RegType,
    elem3: RegType
  )

  // Number of instructions in the file (need a way for this to be dynamic)
  val num_instructions = 1311
  val num_bits = 24
  val pixel_rows = 100
  val pixel_columns = 100
  val registers = 16

  def main(args: Array[String]): Unit = {
    val inst_host = loadCSV1D[InstBit](s"$DATA/load_csv_test_multiple.csv")
    val inst_dram = DRAM[InstBit](num_instructions, num_bits)

    setMem(inst_dram, inst_host)

    val out = DRAM[Int](num_instructions)

    Accel {
      val inst_sram = SRAM[InstBit](num_instructions, num_bits)
      inst_sram load inst_dram

      val internal_out = SRAM[Int](num_instructions)

      Foreach (0 until num_instructions) { i =>
        val a_val = inst_sram(i, 7)
        val gg = inst_sram(i, 8) * 2 + inst_sram(i, 9)
        val oo = inst_sram(i, 10) * 2 + inst_sram(i, 11)
        val src1 = inst_sram(i, 12) * 8 + inst_sram(i, 13) * 4 + inst_sram(i, 14) * 2 + inst_sram(i, 15)
        val src2 = inst_sram(i, 16) * 8 + inst_sram(i, 17) * 4 + inst_sram(i, 18) * 2 + inst_sram(i, 19)
        val dest = inst_sram(i, 20) * 8 + inst_sram(i, 21) * 4 + inst_sram(i, 22) * 2 + inst_sram(i, 23)

        val inst = Instruction(0, a_val.to[OneBitType], gg.to[TwoBitsType], oo.to[TwoBitsType], src1.to[RegLabelType], src2.to[RegLabelType], dest.to[RegLabelType])

        val inst_src1 = inst.src1
        internal_out(i) = inst_src1.to[Int]
      }

      out store internal_out
    }

    val result = getMem(out)

    val gold = Array[Int] (0,3,7,11,15,3,7,11,15,3,7,10,14,2,6,10,14,2,6,10,14,2,5,9,13,1,5,9,13,1,5,9,13,0,4,8,12,0,4,8,12,0,4,7,11,15,3,7,11,15,3,7,11,15,2,6,10,14,2,6,10,14,2,6,10,13,1,5,9,13,1,5,9,13,1,4,8,12,0,4,8,12,0,4,8,12,15,3,7,11,15,3,7,11,15,3,7,10,14,2,6,10,14,2,6,10,14,1,5,9,13,1,5,9,13,1,5,9,12,0,4,8,12,0,4,8,12,0,4,7,11,15)

    // temporary for basic asm tests
    print(result)
    assert(result == gold)
  }
}