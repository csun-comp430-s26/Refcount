# C-Heap Language Documentation

## Project Summary

**Language name:** C-Heap

**Compiler target language:** C

**Team members:** 
- Erick Espinoza
- Christopher Casas

**Repository:** https://github.com/csun-comp430-s26/Refcount

**Overview:**
C-Heap is a language focused on structs, heap allocation, and automatic memory reclamation through reference counting. The language is designed to be easy to read while still supporting meaningful data structures such as linked lists and nested structs. C-Heap compiles into C.

## Why This Language?

Chris: While looking through all the proposals, one of the requirements I gave myself was an easy to understand syntax when writing the language. I really disliked the way that S-expressions looked so I avoided any proposals that utilized them. The idea that all the structs are heap-allocated also interested me since it would give me an opportunity to understand heaps more. I also enjoy C as a language, so making a language that compiles to C sounded neat.

Erick: 

## Code Snippets

TODO

## Known Limitations

- Structs are immutable (by design)
- Structs are heap allocated
- All structs are heap allocated
- No strings, characters, floats, or arrays
- No generics, interfaces, type inferences, or function types

## Lessons Learned

### Would you design anything differently?

Erick:

Chris: 

### Would you choose a different development tool?

Erick:

Chris: I personally used VS Code, which was effective enough to not make me want to switch to a much more powerful IDE

### Would you choose a different target language?

Erick:

Chris: C is one of my favorite languages and one of the reasons I chose this langauge, so no.

### Would you change anything about how you communicated within the group

Erick:

Chris: Communication was a bit rocky at the start as there was some confusion on who was in our group, but overall it worked out fine. We agreed on what each person is working on and then executed it effectively.

## Building the Compiler

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
