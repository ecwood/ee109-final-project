import spatial.dsl._

@spatial class RayTracer extends SpatialTest {


  def main(args: Array[String]): Unit = {
    val width = 400
    val height = 400
    val image = (0::width, 0::height){(i,j) => 0}

    val img = DRAM[T](width, height)
    val imgOut = DRAM[T](width, height)

    setMem(img, image)
    
    Accel {
      
      
    }

  }
}
