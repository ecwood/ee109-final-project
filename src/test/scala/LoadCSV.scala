import spatial.dsl._

@spatial class LoadCSV extends SpatialTest {
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
  val num_instructions = 1
  val num_bits = 24
  val pixel_rows = 100
  val pixel_columns = 100
  val registers = 16

  def main(args: Array[String]): Unit = {
    val inst_host = loadCSV1D[InstBit](s"$DATA/load_csv_test_1.csv")
    val inst_dram = DRAM[InstBit](num_instructions, num_bits)

    setMem(inst_dram, inst_host)

    val out = ArgOut[RegLabelType]

    Accel {
      val inst_sram = SRAM[InstBit](num_instructions, num_bits)
      inst_sram load inst_dram

      val first_a = inst_sram(0, 7)
      val first_gg = inst_sram(0, 8) * 2 + inst_sram(0, 9)
      val first_oo = inst_sram(0, 10) * 2 + inst_sram(0, 11)
      val first_src1 = inst_sram(0, 12) * 8 + inst_sram(0, 13) * 4 + inst_sram(0, 14) * 2 + inst_sram(0, 15)
      val first_src2 = inst_sram(0, 16) * 8 + inst_sram(0, 17) * 4 + inst_sram(0, 18) * 2 + inst_sram(0, 19)
      val first_dest = inst_sram(0, 20) * 8 + inst_sram(0, 21) * 4 + inst_sram(0, 22) * 2 + inst_sram(0, 23)

      val inst = Instruction(0, first_a.to[OneBitType], first_gg.to[TwoBitsType], first_oo.to[TwoBitsType], first_src1.to[RegLabelType], first_src2.to[RegLabelType], first_dest.to[RegLabelType])

      val inst_src1 = inst.src1
      out := inst_src1
    }

    val result = getArg(out)

    // temporary for basic asm tests
    print(result)
    assert(result == 6)
  }
}