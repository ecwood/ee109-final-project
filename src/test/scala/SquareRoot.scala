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
  val iterations = 31

  def main(args: Array[String]): Unit = {
    val out = ArgOut[RegType]

    Accel {
      val temp = 26.0.to[RegType]
      val saved_vals = SRAM[RegType](iterations)

      val actual_square_root = Reg[RegType](0.0)

      Foreach (1 until (iterations + 1)) { square =>
        val squared = (square * square).to[RegType]
        val square_cast = square.to[RegType]
        val divided = (temp.to[SubType] / squared.to[SubType]).to[RegType]
        val shfted = (divided.to[SubType] - 1.0).to[RegType]
        val shfted_sqed = (shfted.to[SubType] * shfted.to[SubType]).to[RegType]
        val shfted_tred = (shfted.to[SubType] * shfted.to[SubType] * shfted.to[SubType]).to[RegType]
        val sqrt_elem1 = 1.0.to[RegType]
        val sqrt_elem2 = (shfted.to[SubType] / 2.0).to[RegType]
        val sqrt_elem3 = (shfted_sqed.to[SubType] / 4.0).to[RegType]
        val sqrt_elem4 = (shfted_tred.to[SubType] / 16.0).to[RegType]
        val sqrt_series = (sqrt_elem1.to[SubType] + sqrt_elem2.to[SubType] - sqrt_elem3.to[SubType] + sqrt_elem4.to[SubType]).to[RegType]
        val square_root = (square_cast.to[SubType] * sqrt_series.to[SubType]).to[SubType]
        val next = (square_cast.to[SubType] + 1.to[SubType]).to[RegType]
        val next_squared = (next.to[SubType] * next.to[SubType]).to[RegType]
        val next_divided = (temp.to[SubType] / next_squared.to[SubType]).to[RegType]
        val save = (divided >= 1 && (next_divided < 1)).to[RegType]
        val value_to_save = (square_root.to[RegType] * save).to[RegType]
        saved_vals(square - 1) = value_to_save.to[RegType]
      }

      Reduce (actual_square_root) (iterations by 1) { sqrt_i =>
        saved_vals(sqrt_i)
      } {_+_}

      out := actual_square_root
    }

    val result = getArg(out)

    // temporary for basic asm tests
    print(result)
    assert(1 == 1)
  }
}