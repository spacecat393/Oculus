/*
 * Copyright (c) 2012-2017 The ANTLR Project. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 */

package repack.antlr.v4.runtime.tree.xpath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import repack.antlr.v4.runtime.ParserRuleContext;
import repack.antlr.v4.runtime.tree.ParseTree;
import repack.antlr.v4.runtime.tree.Tree;
import repack.antlr.v4.runtime.tree.Trees;

public class XPathRuleElement extends XPathElement {
	protected int ruleIndex;
	public XPathRuleElement(String ruleName, int ruleIndex) {
		super(ruleName);
		this.ruleIndex = ruleIndex;
	}

	@Override
	public Collection<ParseTree> evaluate(ParseTree t) {
				// return all children of t that match nodeName
		List<ParseTree> nodes = new ArrayList<ParseTree>();
		for (Tree c : Trees.getChildren(t)) {
			if ( c instanceof ParserRuleContext ) {
				ParserRuleContext ctx = (ParserRuleContext)c;
				if ( (ctx.getRuleIndex() == ruleIndex && !invert) ||
					 (ctx.getRuleIndex() != ruleIndex && invert) )
				{
					nodes.add(ctx);
				}
			}
		}
		return nodes;
	}
}
