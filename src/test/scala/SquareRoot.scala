import spatial.dsl._

@spatial class SquareRoot extends SpatialTest {
  type RegType = FixPt[TRUE, _24, _8]
  type InstructionFixed = FixPt[FALSE, _24, _0]
  type InstBit = FixPt[FALSE, _8, _0]
  type SubType = FixPt[TRUE, _23, _8]

  @struct class Vector3(
    x: RegType,
    y: RegType,
    z: RegType
  )
  def main(args: Array[String]): Unit = {

    val out = ArgOut[RegType]

    Accel {
      val temp = 26.0.to[RegType]
      val saved_vals = SRAM[RegType](31)

      Foreach (1 until 32) { square =>
        val squared = (square * square).to[SubType]
        val divided = temp.to[SubType] / squared
        val shfted = divided.to[SubType] - 1.0
        val square_root = square.to[SubType] * (1.0 + divided / 2.0 - (divided * divided) / 8.0 + (divided * divided * divided) / 16.0)
        val prev = square.to[SubType] - 1.0.to[SubType]
        val save = (divided >= 1 && (temp.to[SubType] / (prev * prev).to[SubType] < 1)).to[RegType]
        val value_to_save = (square_root.to[RegType] * save).to[RegType]
        saved_vals(square - 1) = value_to_save
      }

      val sqrt_ans = 0.0.to[RegType]
      Reduce (sqrt_ans) (0 until 31) { sq =>
        saved_vals(sq)
      }{_+_}

      out := sqrt_ans
    }

    val result = getArg(out)

    // temporary for basic asm tests
    printArray(result)
    assert(1 == 1)
  }
}