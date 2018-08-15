# Scala-Building-Modules
In building Scala modules note the following:
  - Classes can be divided into singleton objects 
  - This approach makes testing easy as we basically modularize each class into mocks
  
```
abstract class Food(val name:String) {
  override def toString = name
  def createFood
}

abstract trait CookBook{
  override def toString = "yes"
  def getStatus: String
}

class Recipe(val name: String, val ingredients: List[String], freeze: Boolean, expiryDate: String, val instructions: String, val packaging: String) {
  import Recipe._
  def buildRecipe = microwaveRecipe(packaging, freeze, expiryDate, name)
}

object Recipe {
  def microwaveRecipe(packaging: String, freeze: Boolean, expiryDate: String, foodType: String):Unit = {
    println(s"About this food ${foodType} = This food is packaged using ${packaging}.")
    println(s"Freeze = ${freeze}.")
    println(s"Expiry date = ${expiryDate}.")
  }
}

object Apple extends Food("Apple") {
  def createFood = {
    println(name)
  }
}

object Pear extends Food("Pear") {
  def createFood = {
    println(name)
  }
}

object FruitSalad extends Recipe("fruit salad", List("pear","melon","spinach"), true, "12/12/12", "just mix", "bowl")

var korma:Recipe = new Recipe("korma", List("rice", "tomatoes", "curry powder"), true, "12/12/12", "instructions", "paper and foil")
println(korma.buildRecipe)

Apple.createFood

object SimpleDatabase {
  def allFoods = List(Apple, Pear)
  def foodNamed(name: String): Option[Food] = 
    allFoods.find(_.name == name)
  def allRecipes: List[Recipe] = List(FruitSalad)
  case class FoodCategory(name: String, foods: List[Food])
  private var categories = List(FoodCategory("fruits", List(Apple, Pear)), FoodCategory("misc", List(Apple, Pear)))
}

object SimpleBrowser {
  def recipesUsing(food: Food) = SimpleDatabase.allRecipes.filter(recipe => recipe.ingredients.contains(food))
}
```
