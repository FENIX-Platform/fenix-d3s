package org.fao.fenix.d3s.search.bl.aggregation.operator;


import java.util.*;

import org.fao.fenix.d3s.msd.dto.common.ValueOperator;
import org.fao.fenix.d3s.search.SearchStep;
import org.fao.fenix.d3s.search.dto.SearchFilter;
import org.fao.fenix.d3s.server.tools.spring.SpringContext;

public abstract class Operator extends SearchStep {
    protected Map<String,Integer> columnParametersIndex = new HashMap<String, Integer>();
    protected Map<String,Object> aggregationParametersValue = new HashMap<String, Object>();

    public void init(ValueOperator operatorInfo) throws Exception {
        //Define fixed value and other business parameters
        Map<String,Object> fixedParameters = operatorInfo.getFixedParameters();
        if (fixedParameters!=null)
            aggregationParametersValue.putAll(fixedParameters);

        String[] keys = getBusinessParametersName();
        Map<String,Object> businessParameters = getFlow().getBusinessParameters();
        if (businessParameters!=null && keys!=null && keys.length>0)
            for (int c=0; c<keys.length; c++)
                aggregationParametersValue.put(keys[c],businessParameters.get(keys[c]));

        //Define column parameters index
        keys = getColumnParametersName();
        if (keys!=null && keys.length>0)
            for (int c=0; c<keys.length; c++)
                for (int i=0; i<structure.length; i++)
                    if (structure[i].getColumnId().equals(keys[c])) {
                        columnParametersIndex.put(keys[c],i);
                        break;
                    }
    }

    protected abstract void init(SearchFilter filter) throws Exception;
    public abstract void reset();
    public abstract void evaluate(Object[] row);
    public abstract Number getFinalResult();

    public static Operator getInstance(SearchStep source, ValueOperator operatorInfo) throws Exception {
        Operator instance = (Operator)SpringContext.getBean(operatorInfo.getImplementation());
        if (source!=null)
            instance.cloneResult(source);
        instance.init(operatorInfo);
        return instance;
    }

    public static String[] getColumnParametersName(ValueOperator operatorInfo) throws Exception {
        Operator instance = (Operator)SpringContext.getBean(operatorInfo.getImplementation());
        return instance.getColumnParametersName();
    }
    public static String[] getBusinessParametersName(ValueOperator operatorInfo) throws Exception {
        Operator instance = (Operator)SpringContext.getBean(operatorInfo.getImplementation());
        return instance.getBusinessParametersName();
    }

    //utils
    public String[] getColumnParametersName() throws Exception {
        OperatorColumns columnsAnnotation = this.getClass().getAnnotation(OperatorColumns.class);
        return columnsAnnotation!=null ? (columnsAnnotation.value()!=null ? columnsAnnotation.value() : new String[0]) : null;
    }
    public String[] getBusinessParametersName() throws Exception {
        OperatorBusiness businessAnnotation = this.getClass().getAnnotation(OperatorBusiness.class);
        return businessAnnotation!=null ? (businessAnnotation.value()!=null ? businessAnnotation.value() : new String[0]) : null;
    }


}