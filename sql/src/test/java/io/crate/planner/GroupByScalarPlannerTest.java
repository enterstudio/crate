package io.crate.planner;


import io.crate.test.integration.CrateDummyClusterServiceUnitTest;
import io.crate.testing.SQLExecutor;
import org.junit.Before;
import org.junit.Test;

public class GroupByScalarPlannerTest extends CrateDummyClusterServiceUnitTest {

    private SQLExecutor e;

    @Before
    public void prepare() {
        e = SQLExecutor.builder(clusterService)
            .enableDefaultTables()
            .build();
    }

    @Test
    public void testGlobalAggregationPlan() throws Exception {
        Merge globalAggregate = e.plan("select count(name) from users");
        Collect collect = (Collect) globalAggregate.subPlan();
        RoutedCollectPhase collectPhase = ((RoutedCollectPhase) collect.collectPhase());

        assertEquals(CountAggregation.LongStateType.INSTANCE, collectPhase.outputTypes().get(0));
        assertThat(collectPhase.maxRowGranularity(), is(RowGranularity.DOC));
        assertThat(collectPhase.projections().size(), is(1));
        assertThat(collectPhase.projections().get(0), instanceOf(AggregationProjection.class));
        assertThat(collectPhase.projections().get(0).requiredGranularity(), is(RowGranularity.SHARD));

        MergePhase mergePhase = globalAggregate.mergePhase();

        assertEquals(CountAggregation.LongStateType.INSTANCE, Iterables.get(mergePhase.inputTypes(), 0));
        assertEquals(DataTypes.LONG, mergePhase.outputTypes().get(0));
    }
}
