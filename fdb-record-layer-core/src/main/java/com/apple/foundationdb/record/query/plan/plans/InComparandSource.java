/*
 * InComparandSource.java
 *
 * This source file is part of the FoundationDB open source project
 *
 * Copyright 2015-2022 Apple Inc. and the FoundationDB project authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.apple.foundationdb.record.query.plan.plans;

import com.apple.foundationdb.annotation.API;
import com.apple.foundationdb.record.Bindings;
import com.apple.foundationdb.record.EvaluationContext;
import com.apple.foundationdb.record.ObjectPlanHash;
import com.apple.foundationdb.record.PlanHashable;
import com.apple.foundationdb.record.query.expressions.Comparisons;
import com.apple.foundationdb.record.query.plan.cascades.Quantifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Source of values for an "IN" query where the values are derived from the comparand of a
 * {@link com.apple.foundationdb.record.query.expressions.Comparisons.Comparison Comparison} object. This is
 * a more generic version of {@link InValuesSource} and {@link InParameterSource} that exists because
 * some comparison objects can have additional logic used to construct the comparand. In principle,
 * it should be safe (if not necessarily the most efficient) to replace one of those other {@link InSource}
 * implementations with this one.
 */
@API(API.Status.INTERNAL)
public class InComparandSource extends InSource {
    @Nonnull
    private static final ObjectPlanHash OBJECT_PLAN_HASH_IN_COMPARAND_SOURCE = new ObjectPlanHash("In-Comparand");

    @Nonnull
    private final Comparisons.Comparison comparison;

    public InComparandSource(@Nonnull final String bindingName, @Nonnull Comparisons.Comparison comparison) {
        super(bindingName);
        this.comparison = comparison;
    }

    @Override
    public int planHash(@Nonnull final PlanHashMode mode) {
        return PlanHashable.objectsPlanHash(mode, baseHash(mode, OBJECT_PLAN_HASH_IN_COMPARAND_SOURCE), comparison);
    }

    @Override
    public boolean isSorted() {
        return false;
    }

    @Override
    public boolean isReverse() {
        return false;
    }

    @Nonnull
    @Override
    public String valuesString() {
        return comparison.typelessString();
    }

    @Override
    protected int size(@Nonnull final EvaluationContext context) {
        return getValues(context).size();
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    protected List<Object> getValues(@Nullable final EvaluationContext context) {
        return (List<Object>)comparison.getComparand(null, context);
    }

    @Nonnull
    @Override
    public RecordQueryInJoinPlan toInJoinPlan(@Nonnull final Quantifier.Physical innerQuantifier) {
        return new RecordQueryInComparandJoinPlan(innerQuantifier, this, Bindings.Internal.CORRELATION);
    }

    @Nonnull
    public Comparisons.Comparison getComparison() {
        return comparison;
    }

    @Nonnull
    @Override
    public String toString() {
        return getBindingName() + " " + comparison;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final InComparandSource inComparandSource = (InComparandSource)o;
        if (!getBindingName().equals(inComparandSource.getBindingName())) {
            return false;
        }
        return comparison.equals(inComparandSource.comparison);
    }

    @Override
    public int hashCode() {
        return comparison.hashCode();
    }
}
