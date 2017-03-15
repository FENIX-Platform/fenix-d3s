package org.fao.fenix.d3s.msd.find.engine;


import org.fao.fenix.commons.find.dto.condition.ConditionFilter;
import org.fao.fenix.commons.find.dto.filter.StandardFilter;

import java.util.Collection;

public interface SearchEngine {

    public Collection<String> getUids( ConditionFilter... filter) throws Exception;
}