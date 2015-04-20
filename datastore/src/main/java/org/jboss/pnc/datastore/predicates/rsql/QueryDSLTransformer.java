package org.jboss.pnc.datastore.predicates.rsql;

import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.path.EnumPath;
import com.mysema.query.types.path.PathBuilder;
import com.mysema.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

/**
 * This is a QueryDSL implementation specific path transformer. Unfortunately {@link com.mysema.query.types.path.SimplePath}
 * does not allow using Enum types (they are not Strings) nor like operators (those in turn are from {@link com.mysema.query.types.path.StringPath}).
 *
 * After changing implementation to Deltaspike (NCL-738) - this part might be safely removed.
 */
public class QueryDSLTransformer<Entity> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    BooleanExpression createNodeTransformer(PathBuilder<Entity> pathBuilder, String operand, List<String> arguments, String operation) {
        try {
            PathMetadata<?> metadata = pathBuilder.get(operand).getMetadata();
            switch(metadata.getPathType()) {
                case PROPERTY:
                    Field field = ReflectionUtils.getFieldOrNull(metadata.getParent().getType(), metadata.getName());
                    if(field == null) {
                        // Fall back to previous implementation
                        break;
                    }
                    logger.debug("Processing an enum field: {}", field);
                    // Unfortunately Query DSL has poor support for Enums. We need to explicitly create proper Path
                    // To give it a hint about the types.
                    Class<?> fieldType = field.getType();
                    if(Enum.class.isAssignableFrom(fieldType)) {
                        EnumPath enumPath = new EnumPath(field.getType(), metadata);
                        Enum enumValue = Enum.valueOf((Class) field.getType(), arguments.get(0));
                        logger.debug("Enum value {}", enumValue);

                        Method m = enumPath.getClass().getMethod(operation, Object.class);
                        return (BooleanExpression) m.invoke(enumPath, enumValue);
                    }
                    //TODO: NCL-755 "Like" implementation goes here
                    //else if (String.class.isAssignableFrom(field.getType())) {
                    //}
                    break;
            }

            PathBuilder<Object> builder = pathBuilder.get(operand);
            Method m = null;
            if(arguments.size() == 1) {
                m = builder.getClass().getMethod(operation, Object.class);
                return (BooleanExpression) m.invoke(builder, arguments.get(0));
            } else {
                m = builder.getClass().getMethod(operation, Collection.class);
                return (BooleanExpression) m.invoke(builder, arguments);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Can not build query", e);
        }
    }
}
