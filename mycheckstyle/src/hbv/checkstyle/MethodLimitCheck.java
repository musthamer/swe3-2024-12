package hbv.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

public class MethodLimitCheck extends AbstractCheck {
    private int max = 3;

    @Override
    public int[] getDefaultTokens() {
        return new int[]{TokenTypes.CLASS_DEF, TokenTypes.INTERFACE_DEF};
    }

    @Override
    public int[] getRequiredTokens() {
        return getDefaultTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return getDefaultTokens();
    }

    @Override
    public void visitToken(DetailAST ast) {
        DetailAST block = ast.findFirstToken(TokenTypes.OBJBLOCK);
        int methodDefs = block.getChildCount(TokenTypes.METHOD_DEF);
        if (methodDefs > max) {
            log(ast.getLineNo(), "Too many methods: " + methodDefs + "/" + max);
        }
    }

    public void setMax(int max) {
        this.max = max;
    }
}

