
# Meal Planner by Jetbrains Academy

This project is implemented according to documentation provided by Jetbrains Academy. It's written in Java and uses JDBC to manage PostgreSQL database instance. Other functionalities of program can be found in Scenario section.


## About

Every day, people face a lot of difficult choices: for example, what to prepare for breakfast, lunch, and dinner? Are the necessary ingredients in stock? With the Meal Planner, this can be quick and painless! You can make a database of categorized meals and set the menu for the week. This app will also help create and store shopping lists based on the meals so that no ingredient is missing.
(Provided by Jetbrains Academy)

## Scenario

Stage 1 - Add meals (Start with add meal function)

```
Which meal do you want to add (breakfast, lunch, dinner)?
> lunch
Input the meal's name:
> salad
Input the ingredients:
> lettuce,tomato,onion,cheese,olives

Category: lunch
Name: salad
Ingredients:
lettuce
tomato
onion
cheese
olives
The meal has been added!
```

Stage 2 - Create a Menu (Add new meals and show meals function)

```
What would you like to do (add, show, exit)?
> add
Which meal do you want to add (breakfast, lunch, dinner)?
> breakfast
Input the meal's name:
> oatmeal
Input the ingredients:
> oats, milk, banana, peanut butter
The meal has been added!
What would you like to do (add, show, exit)?
> show

Category: breakfast
Name: oatmeal
Ingredients:
oats
milk
banana
peanut butter

What would you like to do (add, show, exit)?
> exit
Bye!

```
Stage 3 - Database Storage (Storing meals in database)

```
What would you like to do (add, show, exit)?
> exit
Bye!

What would you like to do (add, show, exit)?
> show

Category: breakfast
Name: oatmeal
Ingredients:
oats
milk
banana
peanut butter

```
Stage 4 - Show saved meals (Retrieving meals from database and priting by category)

```
Which category do you want to print (breakfast, lunch, dinner)?
> breakfast
Category: breakfast
Name: oatmeal
Ingredients:
oats
milk
banana
peanut butter
What would you like to do (add, show, exit)?
> show
Which category do you want to print (breakfast, lunch, dinner)?
> lunch
Category: lunch

Name: salad
Ingredients:
lettuce
tomato
onion
cheese
olives

Name: omelette
Ingredients:
eggs
milk
cheese

What would you like to do (add, show, exit)?
> show
Which category do you want to print (breakfast, lunch, dinner)?
> brunch
Wrong meal category! Choose from: breakfast, lunch, dinner.
> dinner
No meals found.
What would you like to do (add, show, exit)?
> exit
Bye!
```

Stage 5 - Weekly plan (Planning meals for whole week while selecting from menu)

```
What would you like to do (add, show, plan, exit)?
> plan
Monday
oatmeal
sandwich
scrambled eggs
yogurt
Choose the breakfast for Monday from the list above:
> yogurt
avocado egg salad
chicken salad
sushi
tomato salad
wraps
Choose the lunch for Monday from the list above:
> tomato salad
beef with broccoli
pesto chicken
pizza
ramen
tomato soup
Choose the dinner for Monday from the list above:
> spaghetti
This meal doesn’t exist. Choose a meal from the list above.
> ramen
Yeah! We planned the meals for Monday.

Tuesday
oatmeal
sandwich
scrambled eggs
yogurt
Choose the breakfast for Tuesday from the list above:
> oatmeal

*** list shortened ***
```

Stage 6 - Shopping list (Creating a shopping list for the meals in the plan)

```
*** if there is already a plan in DB *** 

What would you like to do (add, show, plan, save, exit)?
> save
Input a filename:
> shoppinglist.txt
Saved!
What would you like to do (add, show, plan, save, exit)?
> exit
Bye!

*** Content of shoppinglist.txt ***

eggs
tomato x3
beef
broccoli
salmon
chicken x2

*** if there is no plan in DB ***

What would you like to do (add, show, plan, save, exit)?
> save
Unable to save. Plan your meals first.
What would you like to do (add, show, plan, save, exit)?
> exit
Bye!
```

## License

[MIT](https://choosealicense.com/licenses/mit/)
