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

Erick: I liked this language because I have used C before in my operating system class so it seemed great to create a complier for it. The reference counting aspect seemed like a challenge to implement as well. Heap allocation has always interested me since it requires variables to be allocated and deallocated for more efficient way of handling memory leaks. 
## Code Snippets

> The tokenizer checks for <=, >=, ==, and != before checking any single-characters. Otherwise, symbols like <= would be read as the less than token and then fail right after.
```java
  if (position + 1 < input.length()) {                                                                                                                                         
      char next = input.charAt(position + 1);                                                                                                                                  
      if (c == '<' && next == '=') {                                                                                                                                           
          position += 2;                                                                                                                                                       
          return Optional.of(new LessEqualToken());                                                                                                                            
      }                                                                                                                                                                        
      if (c == '>' && next == '=') {                                                                                                                                           
          position += 2;                                                                                                                                                       
          return Optional.of(new GreaterEqualToken());                                                                                                                         
      }                                                                                                                                                                        
      if (c == '=' && next == '=') {                                                                                                                                           
          position += 2;                                                                                                                                                       
          return Optional.of(new EqualEqualToken());
      }                                                                                                                                                                        
      if (c == '!' && next == '=') {                                                                                                                                           
          position += 2;                                                                                                                                                       
          return Optional.of(new NotEqualToken());                                                                                                                             
      }                                                                                                                                                                        
  }
```
> Code from Typechecker for checking the while statments. In this case we had to create a Bool variable called 'inLoop' that would be passed to determine if the code is in a loop or not. We could have went with the route of a global variable but was bad in practice to do. This code will use a helper function that checks if the statement conditon is a valid bool type and then called the check statement function for the body inside and set the inLoop to 'true' when a while loop is occuring. In relation to this function there is the Break condition in the function that checks statments and then sets the 'inLoop' variable back to false to state that the loop has been broken. 
```java
    public static Map<Identifier, Type> typecheckWhile(final WhileStmt stmt, final Map<Identifier, Type> typeEnv,
            final Map<String, Map<String, Type>> structEnv,
            final Map<String, FuncDef> functionEnv,
            final boolean inLoop, final Type expectedReturnType)
            throws TypeErrorException {
        assertTypesEqual(new BoolType(), typeof(stmt.condition(), typeEnv, structEnv, functionEnv));
        typecheckStmt(stmt.body(), typeEnv, structEnv, functionEnv, true, expectedReturnType);
        return typeEnv;
    }
// code snipet from checkType in statments related to while statment.
        } else if (stmt instanceof BreakStmt) {
            if (inLoop) {
                return typeEnv;
            } else {
                throw new TypeErrorException("Break outside of Loop");
            }

```
> This is another fucntion from the Typechecker. This fucntion is used as a helper function to check if the fields in the struct definition are valid. This check function ensures that the fields being added to a struct are of a valid type and checks that void is not a valid type for a variable being declared. As well it checks if other structs declared can be used in the struct definition by checking the Map are the Struct. We use a Map<String, Map<String, Type>> type because the structs can have multiple variables declared so an inside Map is needed to keep track of them. The last check throws and error if none of the valid types was passed in.  
```java
 public static void assertValidFieldType(
            final Type type,
            final Map<String, Map<String, Type>> structEnv)
            throws TypeErrorException {

        if (type instanceof IntType || type instanceof BoolType) {
            return;
        }

        if (type instanceof VoidType) {
            throw new TypeErrorException("Struct fields cannot have type void");
        }

        if (type instanceof StructType structType) {
            if (!structEnv.containsKey(structType.name())) {
                throw new TypeErrorException("Unknown struct type: " + structType.name());
            }
            return;
        }

        throw new TypeErrorException("Invalid field type: " + type);
    }
```
> This function is from the Token file. This function in hindsight could have been slpit or changed since we went with 'Identifier' as a catch all for all named variables, structs, and functions. In the last else statment we check if no reserved word was use then the passed in Token would become an Identifier Token which could be used to represent anything. In the Parser it got confusing changing 'identifier' with 'name' and should have stuck to one when coding to keep a sense of consistency. 
 public Optional<Token> readIdentifierOrReservedWord() {
        char c = input.charAt(position);
        if (Character.isLetter(c)) {
            String identifierOrReservedWord = "" + c;
            position++;

            while (position < input.length() &&
                    Character.isLetterOrDigit(c = input.charAt(position))) {
                identifierOrReservedWord += c;
                position++;
            }

            if (identifierOrReservedWord.equals("true")) {
                return Optional.of(new TrueToken());
            } else if (identifierOrReservedWord.equals("false")) {
                return Optional.of(new FalseToken());
            } else if (identifierOrReservedWord.equals("void")) {
                return Optional.of(new VoidToken());
            } else if (identifierOrReservedWord.equals("while")) {
                return Optional.of(new WhileToken());
            } else if (identifierOrReservedWord.equals("struct")) {
                return Optional.of(new StructToken());
            } else if (identifierOrReservedWord.equals("null")) {
                return Optional.of(new NullToken());
            } else if (identifierOrReservedWord.equals("new")) {
                return Optional.of(new NewToken());
            } else if (identifierOrReservedWord.equals("println")) {
                return Optional.of(new PrintlnToken());
            } else if (identifierOrReservedWord.equals("return")) {
                return Optional.of(new ReturnToken());
            } else if (identifierOrReservedWord.equals("if")) {
                return Optional.of(new IfToken());
            } else if (identifierOrReservedWord.equals("else")) {
                return Optional.of(new ElseToken());
            } else if (identifierOrReservedWord.equals("bool")) {
                return Optional.of(new BoolToken());
            } else if (identifierOrReservedWord.equals("break")) {
                return Optional.of(new BreakToken());
            } else if (identifierOrReservedWord.equals("func")) {
                return Optional.of(new FuncToken());
            } else if (identifierOrReservedWord.equals("int")) {
                return Optional.of(new IntToken());
            } else {

                return Optional.of(new IdentifierToken(identifierOrReservedWord));
            }
        } else {
            return Optional.empty();
        }

    }
```
> This is function is from the Parsers file. Here we are creating a function for the grammar for struct definitions. There is a check to see if a struct token is present before parsing the rest of the token. Then we extract the name of the struct to be used and we again use the catch all Idenitifier Token. We check for the brackets and the list of potential parameters by checking the right brace token has not been read. As we can see in this example we use 'name' for the struct definition which should have been used for all but in some classes in out Parser folder we also used Idenitfier. This could be a result of each of use having different naming styles and a formal way to name these varaibles could have been used. 
```java
    // structdef ::= `struct` structname `{` (param `;`)* `}`
    public ParseResult<StructDef> parseStructDef(final int startPos) throws ParseException {
        if (!(getToken(startPos) instanceof StructToken)) {
            throw new ParseException("Expected 'struct' at position " + startPos);
        }
        final Token nameTok = getToken(startPos + 1);
        if (!(nameTok instanceof IdentifierToken id)) {
            throw new ParseException("Expected struct name at position " + (startPos + 1));
        }
        assertTokenHereIs(startPos + 2, new LeftBraceToken());
        final List<Param> fields = new ArrayList<>();
        int pos = startPos + 3;
        while (!(getToken(pos) instanceof RightBraceToken)) {
            final ParseResult<Param> p = parseParam(pos);
            fields.add(p.result());
            assertTokenHereIs(p.nextPos(), new SemicolonToken());
            pos = p.nextPos() + 1;
        }
        return new ParseResult<>(new StructDef(id.name(), fields), pos + 1);
    }
```


## Known Limitations

- Structs are immutable (by design)
- Structs are heap allocated
- All structs are heap allocated
- No strings, characters, floats, or arrays
- No generics, interfaces, type inferences, or function types

## Lessons Learned

### Would you design anything differently?

Erick: I would have made sure the way we name variables was the same accross all files. An example is for some files we used 'name' for variable name or we would use 'identifier' since that was the generic name for variables we used. If we had time to maybe try to use a functional language to develop our complier since from the class examples made it seem like an easier choice to write it in. 

Chris:

### Would you choose a different development tool?

Erick: I have always used VS Code so I feel most comfortable with it since you can add extensions to make it more tailored. 

Chris: I personally used VS Code, which was effective enough to not make me want to switch to a much more powerful IDE

### Would you choose a different target language?

Erick: If I ever did another complier for a language I would say Python since its a dynamic language. 

Chris: C is one of my favorite languages and one of the reasons I chose this langauge, so no.

### Would you change anything about how you communicated within the group

Erick: Each person knew what they had to compelete for work and response between each other was fast and prompt. 

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
