package org.jboss.pnc.datastore.predicates.rsql;

import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.EqualNode;
import cz.jirutka.rsql.parser.ast.GreaterThanNode;
import cz.jirutka.rsql.parser.ast.GreaterThanOrEqualNode;
import cz.jirutka.rsql.parser.ast.InNode;
import cz.jirutka.rsql.parser.ast.LessThanNode;
import cz.jirutka.rsql.parser.ast.LessThanOrEqualNode;
import cz.jirutka.rsql.parser.ast.LogicalNode;
import cz.jirutka.rsql.parser.ast.NoArgRSQLVisitorAdapter;
import cz.jirutka.rsql.parser.ast.Node;
import cz.jirutka.rsql.parser.ast.NotEqualNode;
import cz.jirutka.rsql.parser.ast.NotInNode;
import cz.jirutka.rsql.parser.ast.OrNode;

abstract class RSQLNodeTraveller<T> extends NoArgRSQLVisitorAdapter<T> {

   public abstract T visit(LogicalNode logicalNode);

   public abstract T visit(ComparisonNode logicalNode);

   public T visit(Node node) {
      //remember overloading is chosen based on static type.
      if(node instanceof LogicalNode) {
         return visit((LogicalNode) node);
      } else if(node instanceof ComparisonNode) {
         return visit((ComparisonNode) node);
      } else {
         throw new UnsupportedOperationException("Did you invent 3rd type of the node?");
      }
   }

   @Override
   public T visit(AndNode node) {
      return visit((LogicalNode) node);
   }

   @Override
   public T visit(OrNode node) {
      return visit((LogicalNode) node);
   }

   @Override
   public T visit(EqualNode node) {
      return visit((ComparisonNode) node);
   }

   @Override
   public T visit(NotEqualNode node) {
      return visit((ComparisonNode) node);
   }

   @Override
   public T visit(InNode node) {
      return visit((ComparisonNode) node);
   }

   @Override
   public T visit(NotInNode node) {
      return visit((ComparisonNode) node);
   }

   @Override
   public T visit(GreaterThanOrEqualNode node) {
      return visit((ComparisonNode) node);
   }

   @Override
   public T visit(GreaterThanNode node) {
      return visit((ComparisonNode) node);
   }

   @Override
   public T visit(LessThanOrEqualNode node) {
      return visit((ComparisonNode) node);
   }

   @Override
   public T visit(LessThanNode node) {
      return visit((ComparisonNode) node);
   }
}
