/*
 * Copyright (C) 2016 Reece H. Dunn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.reecedunn.intellij.plugin.xquery.psi.impl.xquery;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.co.reecedunn.intellij.plugin.xquery.ast.xquery.XQueryEQName;
import uk.co.reecedunn.intellij.plugin.xquery.ast.xquery.XQueryPositionalVar;
import uk.co.reecedunn.intellij.plugin.xquery.psi.XQueryVariable;
import uk.co.reecedunn.intellij.plugin.xquery.psi.XQueryVariableProvider;

public class XQueryPositionalVarPsiImpl extends ASTWrapperPsiElement implements XQueryPositionalVar, XQueryVariableProvider {
    public XQueryPositionalVarPsiImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Nullable
    @Override
    public XQueryVariable resolveVariable(XQueryEQName name) {
        PsiElement eqname = findChildByClass(XQueryEQName.class);
        if (eqname != null && eqname.equals(name)) {
            return new XQueryVariable(eqname, this);
        }

        return null;
    }
}
