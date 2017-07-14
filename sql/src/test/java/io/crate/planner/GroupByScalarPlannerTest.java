package io.crate.planner;


import com.google.common.collect.Iterables;
import io.crate.analyze.symbol.Function;
import io.crate.metadata.RowGranularity;
import io.crate.operation.aggregation.impl.CountAggregation;
import io.crate.operation.scalar.arithmetic.ArithmeticFunctions;
import io.crate.planner.node.dql.Collect;
import io.crate.planner.node.dql.MergePhase;
import io.crate.planner.node.dql.RoutedCollectPhase;
import io.crate.planner.projection.AggregationProjection;
import io.crate.planner.projection.EvalProjection;
import io.crate.planner.projection.GroupProjection;
import io.crate.planner.projection.OrderedTopNProjection;
import io.crate.planner.projection.TopNProjection;
import io.crate.test.integration.CrateDummyClusterServiceUnitTest;
import io.crate.testing.SQLExecutor;
import io.crate.types.DataTypes;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class GroupByScalarPlannerTest extends CrateDummyClusterServiceUnitTest {

    private SQLExecutor e;

    @Before
    public void prepare() {
        e = SQLExecutor.builder(clusterService)
            .enableDefaultTables()
            .build();
    }

    @Test
    public void testGroupByWithScalarPlan() throws Exception {
        Merge merge = e.plan("select id + 1 from users group by id");
        Collect collect = (Collect) merge.subPlan();
        RoutedCollectPhase collectPhase = ((RoutedCollectPhase) collect.collectPhase());

        assertEquals(DataTypes.LONG, collectPhase.outputTypes().get(0));
        assertThat(collectPhase.maxRowGranularity(), is(RowGranularity.DOC));
        assertThat(collectPhase.projections().size(), is(2));
        assertThat(collectPhase.projections().get(0), instanceOf(GroupProjection.class));
        assertThat(collectPhase.projections().get(0).requiredGranularity(), is(RowGranularity.SHARD));
        assertThat(collectPhase.projections().get(1), instanceOf(EvalProjection.class));
        assertThat(collectPhase.projections().get(1).outputs().get(0), instanceOf(Function.class));

        MergePhase mergePhase = merge.mergePhase();

        assertEquals(DataTypes.LONG, Iterables.get(mergePhase.inputTypes(), 0));
        assertEquals(DataTypes.LONG, mergePhase.outputTypes().get(0));
    }

    @Test
    public void testGroupByWithMultipleScalarPlan() throws Exception {
        Merge merge = e.plan("select abs(id + 1) from users group by id");
        Collect collect = (Collect) merge.subPlan();
        RoutedCollectPhase collectPhase = ((RoutedCollectPhase) collect.collectPhase());

        assertEquals(DataTypes.LONG, collectPhase.outputTypes().get(0));
        assertThat(collectPhase.maxRowGranularity(), is(RowGranularity.DOC));
        assertThat(collectPhase.projections().size(), is(2));
        assertThat(collectPhase.projections().get(0), instanceOf(GroupProjection.class));
        assertThat(collectPhase.projections().get(0).requiredGranularity(), is(RowGranularity.SHARD));
        assertThat(collectPhase.projections().get(1), instanceOf(EvalProjection.class));
        assertThat(collectPhase.projections().get(1).outputs().get(0), instanceOf(Function.class));

        MergePhase mergePhase = merge.mergePhase();

        assertEquals(DataTypes.LONG, Iterables.get(mergePhase.inputTypes(), 0));
        assertEquals(DataTypes.LONG, mergePhase.outputTypes().get(0));
    }

    @Test
    public void testGroupByScalarWithMultipleColumnArgumentsPlan() throws Exception {
        Merge merge = e.plan("select abs(id + other_id) from users group by id, other_id");
        Merge subplan = (Merge) merge.subPlan();
        Collect collect = (Collect) subplan.subPlan();
        RoutedCollectPhase collectPhase = ((RoutedCollectPhase) collect.collectPhase());
        assertThat(collectPhase.projections().size(), is(1));
        assertThat(collectPhase.projections().get(0), instanceOf(GroupProjection.class));
        assertThat(collectPhase.projections().get(0).requiredGranularity(), is(RowGranularity.SHARD));

        MergePhase mergePhase = subplan.mergePhase();
        assertThat(mergePhase.projections().size(), is(2));
        assertThat(mergePhase.projections().get(0), instanceOf(GroupProjection.class));
        assertThat(mergePhase.projections().get(1), instanceOf(EvalProjection.class));
    }

    @Test
    public void testGroupByScalarArgumentsNotMatched() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("column 'abs(id)' must appear in the GROUP BY clause or be used in an aggregation function");
        e.plan("select abs(id) from users group by id + 1");
    }
    
    @Test
    public void testGroupByScalarNotAllColumnsMatched() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("column 'other_id' must appear in the GROUP BY clause or be used in an aggregation function");
        e.plan("select abs(id), other_id from users group by id");
    }
}
