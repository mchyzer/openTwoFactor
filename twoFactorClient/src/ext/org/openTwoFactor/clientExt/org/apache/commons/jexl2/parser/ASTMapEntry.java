/* Generated By:JJTree: Do not edit this line. ASTMapEntry.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.openTwoFactor.clientExt.org.apache.commons.jexl2.parser;

public
class ASTMapEntry extends JexlNode {
  public ASTMapEntry(int id) {
    super(id);
  }

  public ASTMapEntry(Parser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(ParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=d32fc068a045f517f701e00a03efcab9 (do not edit this line) */
