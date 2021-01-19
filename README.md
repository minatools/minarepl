# minarepl

`minarepl` is a simple bit of Clojure code enabling real-time
interaction with a Mina server via GraphQL. It is not intended as an
application; there is no "main" routine.  It just makes it easy to submit GraphQL queries to a local Mina node from a Clojure REPL.

It uses the [graphql-bulder](https://github.com/retro/graphql-builder)
library to construct queries, and
[hato](https://github.com/gnarroway/hato) HTTP client to submit them.

Queries are stored in [gql/](gql). Most of them are taken directly
from the Mina source code.

The procedure is simple: use `graphql-builder` to read the query
source and construct a query object (setting the variables if need
be); then construct the JSON body for an HTTP message from the query
object; submit the HTTP request; and finally parse the result. That's
it. But the result is Clojure data, wh sh is very easy to work with,
and you can use Clojure libraries to manipulate it.

## Installation

Clone from https://github.com/minatools/minarepl

## Usage

The recommended way to use it is with emacs and
[cider](https://docs.cider.mx/cider/1.0/index.html) or any editor with
integrated REPL support. You can also start up a REPL and use it directly.

Edit `src/mina/repl.clj`. Put  yor public key in the `pub-key` def.

Jack-in to a repl: `C-c C-x C-j C-j`.  Starting at the top, execute the sexps.  Uncomment the line in `(def gql-request ... )` that contains the query you want.  Adjust the variables map to taste.

Construction and submission of the query is wrapped in a `(let )` production.  Add code there to inspect/manipulate the result.

## Examples

...

### Bugs

...

## License

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
