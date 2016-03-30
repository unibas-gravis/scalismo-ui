package scalismo.ui.view.util

class FloatSlider(val minFloat: Float, val maxFloat: Float, val stepFloat: Float) extends FancySlider {

  private def f2i(f: Float): Int = {
    val sf = Math.max(minFloat, Math.min(maxFloat, f))
    Math.round((sf - minFloat) / stepFloat)
  }

  private def i2f(i: Int): Float = {
    minFloat + stepFloat * i
  }

  def floatValue = i2f(value)

  def floatValue_=(newValue: Float) = value = f2i(newValue)

  min = 0
  max = ((maxFloat - minFloat) / stepFloat).toInt
  value = min + (max - min) / 2

  // intended to be overwritten in subclasses if needed
  override def formattedValue(sliderValue: Int): String = {
    f"${i2f(sliderValue)}%1.1f"
  }
}

