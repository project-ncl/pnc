package org.jboss.pnc.rest.repository;


import com.google.common.base.Preconditions;
import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.RSQLParserException;
import cz.jirutka.rsql.parser.ast.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class RSQLDataStoreSelectorAdapter<Entity> implements RSQLAdapter<Entity> {

   private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

   private final Node rootNode;

   private final Map<Class<? extends ComparisonNode>, Transformer<Entity>> operations = new HashMap<>();

   RSQLDataStoreSelectorAdapter(String rsql) throws RSQLParserException {
      operations.put(EqualNode.class, (r, cb, operand, arguments) -> cb.equal(r.get(operand), arguments.get(0)));
      operations.put(NotEqualNode.class, (r, cb, operand, arguments) -> cb.notEqual(r.get(operand), arguments.get(0)));
      operations.put(GreaterThanNode.class, (r, cb, operand, arguments) -> cb.greaterThan(r.get(operand), arguments.get(0)));
      operations.put(GreaterThanOrEqualNode.class, (r, cb, operand, arguments) -> cb.greaterThanOrEqualTo(r.get(operand), arguments.get(0)));
      operations.put(LessThanNode.class, (r, cb, operand, arguments) -> cb.lessThan(r.get(operand), arguments.get(0)));
      operations.put(LessThanOrEqualNode.class, (r, cb, operand, arguments) -> cb.lessThanOrEqualTo(r.get(operand), arguments.get(0)));
      operations.put(InNode.class, (r, cb, operand, arguments) -> r.get(operand).in(arguments));
      operations.put(NotInNode.class, (r, cb, operand, arguments) -> cb.not(r.get(operand).in(arguments)));

      this.rootNode = new RSQLParser().parse(rsql);
   }

   @Override
   public Predicate toPredicate(Root<Entity> r, CriteriaQuery<?> cq, CriteriaBuilder cb) {

      RSQLNodeTraveller<Predicate> visitor = new RSQLNodeTraveller<Predicate>() {

         public Predicate visit(LogicalNode node) {
            logger.info("Parsing LogicalNode {}", node);
            return proceedEmbeddedNodes(node);
         }

         public Predicate visit(ComparisonNode node) {
            logger.info("Parsing ComparisonNode {}", node);
            return proceedSelection(node);
         }

         private Predicate proceedSelection(ComparisonNode node) {
            Transformer<Entity> transformation = operations.get(node.getClass());
            Preconditions.checkArgument(transformation != null, "Operation not supported");

            return transformation.transform(r, cb, node.getSelector(), node.getArguments());
         }

         private Predicate proceedEmbeddedNodes(LogicalNode node) {
            Iterator<Node> iterator = node.iterator();
            if (node instanceof AndNode) {
               return cb.and(visit(iterator.next()), visit(iterator.next()));
            } else if (node instanceof OrNode) {
               return cb.or(visit(iterator.next()), visit(iterator.next()));
            } else {
               throw new UnsupportedOperationException("Logical operation not supported");
            }
         }
      };

      return rootNode.accept(visitor);
   }

}
