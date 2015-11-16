# Enterprise Location Services

A stereotype represents the blue print that dictates how graph artifacts mutate over time , new nodes and relationships are created while still keeping the
graph in a consistent and valid state

**Stereotypes defines:**
  * Specifications for a node and relationship properties
  * Specifications for how nodes are inter*connected

In neo4j relationships are unidirectional, a cardinality of a relationship defines the minimum and maximum outgoing instance of the same relationship.
Example:----
  * Given  BELONGS_TO relationship between a rack and an aisle
  * Can the rack exist
    * Without a BELONGS_TO relation to an aisle? This is dictated by minCardinality.
    * With multiple instances of outgoing BELONGS_TO relations with multiple aisle? This is controlled by maxCardinality.



Markup : ![picture alt](stereotype.png "Store Stereotype")