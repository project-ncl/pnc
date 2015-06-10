package org.jboss.pnc.datastore.predicates.rsql;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;

import java.util.List;
import java.util.stream.Collectors;

class ArgumentHelper {

    private static final BeanUtilsBean beanUtilsBean;

    static {
        beanUtilsBean = new BeanUtilsBean(new ConvertUtilsBean(){
            @Override
            public Object convert(String value, Class clazz) {
                if (clazz.isEnum()){
                    return Enum.valueOf(clazz, value);
                }else{
                    return super.convert(value, clazz);
                }
            }
        });
    }

    public Object getConvertedType(Class<?> selectingClass, String path, String argumentAsString) {
        String[] fields = path.split("\\.");
        Class<?> currentClass = selectingClass;
        for(String field : fields) {
            try {
                currentClass = currentClass.getDeclaredField(field).getType();
            } catch (NoSuchFieldException e) {
                throw new RSQLConverterException("Unable to get class for field " + field, e);
            }
        }
        return beanUtilsBean.getConvertUtils().convert(argumentAsString, currentClass);
    }

    public List<Object> getConvertedType(Class<?> selectingClass, String path, List<String> arguments) {
        return arguments.stream().map(argument -> this.getConvertedType(selectingClass, path, argument)).collect(Collectors.toList());
    }

}
