# Hash Tables

## Overview

In this project, you will have to implement an abstraction over a *phonebook*;
a collections of pairs of type `<`*Full_Name*,*Phone_Number*`>`. Your phonebook
will support **both** name-based search and phone-based search. Here is a
pictorial view of the project:

![High-level view of phonebook](img/phonebook.png "A high-level view of how your phonebook is supposed to work")

To make both types of searches efficient, your phonebook will internally
maintain **a pair of hash tables** from `String`s to `String`s: One will have
the **person's name as a key** and the **phone number as a value**, and the
other one will have the **phone number as a key** and the **name as a value**!
In your simple phonebook, **entry uniqueness** is **guaranteed**: Every person
has **exactly one** phone number, and every phone number is associated with
**exactly one** person.

## Getting Started

As before, this project is provided to you through a GitLab repository. If you
are viewing this on the web, you should clone either the master repository
or your personal repository. After you have cloned the project, you should
study the JavaDocs and source code provided to understand how your methods can
be used (and tested). The classes you have to implement are under the package
`hashes`.

## Provided Code

### Class hierarchy

![UML diagram of code structure](img/hierarchy.png "A UML diagram describing the code structure.")

The above diagram depicts the code structure for the project. Simple lines
reflect one-to-many "has-a" relations, while arrows show "is-a" relations
(derived class, implemented interface, etc.). The top-level class of the
project is `Phonebook`. What is interesting about `Phonebook` is that **it has been
implemented for you!** However, the methods of `Phonebook` depend on methods
of the interface `HashTable`, which is implemented (directly or through
`OpenAddressingHashTable`) by the four classes which you will have to implement.
What *you* will need to do is complete the implementation of these four
classes so that their methods can support the methods of `Phonebook`. The
Release Tests **primarily** test methods of `Phonebook` (approximately 90%
of their code), while a smaller number of tests check if you are implementing
basic hash table functionality correctly (e.g. resizings, see below).

The various methods of `Phonebook` will have to run in *amortized constant time*
(except for `size()` and `isEmpty()`, which should run in *constant* time).
This does *not* take into account the case of an insertion or deletion that
results in a *resizing* of the array; we want amortized constant time
*assuming* that a resizing does **not** occur in that particular operation.
We **will** be checking your source code after submission to make sure you
are **not** implementing the methods **inefficiently** (e.g. logarithmic
complexity, linear complexity, or even worse)!

In practice, the only way you can do this is by **not** consulting the hash
function **at all** for your operations; just **looping over the entire table**
until you either find the element (`remove`, `containsKey`) or you find an
empty position (`put`). While this would indeed allow you to pass the tests,
we will be **checking your submission** to make sure you **consult the
hash function!** Implementing all operations as mentioned above would
constrain them to be *linear time*, which is **unacceptable** for both the
project (*ie*, no credit for this project) and Computer Science **as a whole**.
That said, **not all** of the methods you implement make use of the hash
function, which means that their complexity parameters will necessarily by
different. (Can you find any such methods?)

You should fill in the `public` methods of `SeparateChainingHashTable`,
`LinearProbingHashTable`, `OrderedLinearProbingHashTable`, and
`QuadraticProbingHashTable`. For the last three classes, you might find that
some of the methods have the **exact same source code**. You would then
benefit by making them `protected` methods in the `OpenAddressingHashTable`
class, which those three classes extend!

### Interfaces, `abstract` classes, and the `protected` access modifier

In the code base, you might notice that `HashTable` is a Java *interface*.
On the otehr hand, `OpenAddressingHashTable` is an `abstract` class. Abstract
classes in Java are almost like interfaces, except they are allowed to contain
fields and their members do **not** default to `public`. Similarly to
interfaces, one **cannot** instantiate an `abstract` class.

The choice of making `OpenAddressingHashTable` an `abstract` class is
deliberate; several methods and fields of **all** your Openly Addressed hash
tables are common across **all** of these classes. Therefore, it makes sense
to package them into *one place* and debug them in *one place*. Unfortunately,
Java interfaces do **not** allow for storing fields, but only methods, which
are also implicitly `public`. Essentially, the `HashTable` interface tells us
what kinds of methods any `HashTable` instance **ought** to satisfy. For
example, every `HashTable` instance needs to provide a method called `put`,
with two `String` arguments, `key` and `value`, as well as a return value of
type `Probes`. Refer to the section on *Classes under `utils`* for a short
diatribe on this small class. It should also answer questions of key
containment (`containsKey(String key)`) and queries of its current stored key
count (`size()`) and capacity (`capacity()`).

On the other hand, any Openly Addressed Hash Table needs to have some common
fields and functionality. They all need an array over `KVPair` instances. They
all need to answer queries of size and capacity in $O(1)$. They can all
benefit from an overriding of `toString()`, which we provide for you and is
very useful for debugging[^1]. Therefore, this entire piece of common
functionality can (and should) be packages in **one common place**, and this
place in our code base is `OpenAddressingHashTable`.

You might notice that all the fields and methods of `OpenAddressingHashTable`
besides `toString()` are labeled with the `protected` access modifier.
`protected` essentially means: "visible by derived classes". In more detail,
every *identifier* (name) of a field or a method that has been declared
`protected` in a base class can be *straightforwardly* accessed from a derived
class by its name, without any prepending of base class name or of any other
name. This is very useful for your code! You might notice that
`LinearProbingHashTable`, `OrderedLinearProbingHashTable`, and
`QuadraticProbingHashTable`, all classes that you **must** implement, have
**no** `private` **fields** (but if you would like to add some, please go
right ahead). In fact, our own implementation of the project does not add any
extra fields in the classes (but we **do** use `private` methods for
readability).

This is, of course, **not** a perfect approach towards building this code
base. For example, you might notice that `SeparateChainingHashTable`, the only
collision resolution method you have to implement that is not an Open
Addressing method, *necessarily* has to declare some `private` fields that
we also see in `OpenAddressingHashTable`. Also, somebody can argue that since
`OrderedLinearProbingHashTable` has so many common characteristics (the
*Collision Resolution Methods* section analyzes this in some detail) with
`LinearProbingHashTable`, one could make the former a subclass of the latter.
You can come up with many different approaches of refactoring this code base,
but
**PLEASE DON'T, OTHERWISE YOU MIGHT END UP NOT PASSING ANY OF OUR UNIT TESTS!**
The *only* thing you can do is add your own `private` fields or
methods in the classes you have to implement and, in the case of the three
Open Addressing methods you have to implement, you can move some of the common
code you build as `protected` methods in `OpenAddressingHashTable`. See the
comments at the very end of that class's definition for a relevant prompt.

The **tl;dr** of what you *can* and *cannot* change in the code base is this:
unit tests test `public` functionality and they also need to be aware of type
information at **compile-time**, since Java is a *strongly* typed language.

 * Are you in any way breaking the `public` methods' signatures and/or return types?
 * Are you in any way altering the code base's hierarchy by making classes extend other classes and interfaces?
 
If the answer to **both** of these questions is **no**, you are good, otherwise you are **not** good.

[^1]: So, if you are *persistent* about **not** using the debugger, **at least print your table** before coming to office hours, please.

### Classes under `hashes`

### Classes under `utils`

## Collision Resolution Methods

### Separate Chaining

### Linear Probing

### Ordered Linear Probing

### Quadratic Probing

## Soft vs. Hard Deletion in Openly Addressed Hash Tables

## FAQs


