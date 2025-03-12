package hbv.checkstyle;

import com.puppycrawl.tools.checkstyle.api.*;
import com.puppycrawl.tools.checkstyle.utils.*;

public class MyAnnotationCheck extends AbstractCheck {

  public void beginTree(DetailAST ast) {}

  public int[] getDefaultTokens() {
    return new int[] {TokenTypes.ANNOTATION};
  }

  public int[] getRequiredTokens() {
    return getDefaultTokens();
  }

  public int[] getAcceptableTokens() {
    return getDefaultTokens();
  }

  public void visitToken(DetailAST ast) {
    // ast ist *the* annotation per se (@)
    // concreteAnnotationAst ist the next Token of Type IDENT
    DetailAST concreteAnnotationAst = ast.findFirstToken(TokenTypes.IDENT);
    if (concreteAnnotationAst == null) return;
    String annotationName = concreteAnnotationAst.getText();
    if (annotationName.equals("SuppressWarnings")) {
      log(ast, "No more SuppressWarning-Annotations");
    }
  }
}
