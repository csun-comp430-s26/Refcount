# C-Heap Language Documentation

## Project Summary

**Language name:** C-Heap

**Compiler target language:** C

**Team members:** 
- Erick Espinoza
- Christopher Casas

**Repository:** https://github.com/csun-comp430-s26/Refcount

**Overview:**
TODO

## Why This Language?

Chris: 

Erick: 

## Code Snippets

TODO

## Known Limitations

TODO

## Lessons Learned

TODO

## Building the Compiler

TODO

Prerequisites:

- Java 21 or newer.
- Maven available from PowerShell or a terminal.

Compile and run tests:

```powershell
mvn clean test
```

Generate the JaCoCo coverage report:

```powershell
mvn clean verify
```

Open the coverage report on Windows:

```powershell
start .\target\site\jacoco\index.html
```

## Running the Compiler

Run tests:

```powershell
mvn test
```

## Formal Syntax Definition

```text
var is a variable
structname is the name of a structure
funcname is the name of a function
i is an integer

type ::= `int` | `bool` | `void` | structname

param ::= type var

comma_param ::= [param (`,` param)*]

structdef ::= `struct` structname `{` (param `;`)* `}`

fdef ::= `func` funcname `(` comma_param `)` `:` type `{` stmt* `}`

struct_actual_param ::= var `:` exp

struct_actual_params ::= [struct_actual_param (`,` struct_actual_param)*]

comma_exp ::= [exp (`,` exp)*]

primary_exp ::= i
              | `true`
              | `false`
              | var
              | `null`
              | `(` exp `)`
              | `new` structname `{` struct_actual_params `}`
              | funcname `(` comma_exp `)`

dot_exp ::= primary_exp (`.` var)*

mult_exp ::= dot_exp ((`*` | `/`) dot_exp)*

add_exp ::= mult_exp ((`+` | `-`) mult_exp)*

relational_exp ::= add_exp (`<` add_exp | `<=` add_exp | `>` add_exp | `>=` add_exp)

equals_exp ::= relational_exp ((`==` | `!=`) relational_exp)*

exp ::= equals_exp

stmt ::= type var `=` exp `;`
       | var `=` exp `;`
       | `if` `(` exp `)` stmt [`else` stmt]
       | `while` `(` exp `)` stmt
       | `break` `;`
       | `println` `(` exp `)` `;`
       | `{` stmt* `}`
       | `return` [exp] `;`
       | exp `;`

program ::= structdef* fdef* stmt*
```