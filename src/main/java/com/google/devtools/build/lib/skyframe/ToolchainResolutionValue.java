// Copyright 2017 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.devtools.build.lib.skyframe;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.devtools.build.lib.cmdline.Label;
import com.google.devtools.build.lib.skyframe.serialization.autocodec.AutoCodec;
import com.google.devtools.build.skyframe.SkyFunctionName;
import com.google.devtools.build.skyframe.SkyKey;
import com.google.devtools.build.skyframe.SkyValue;
import java.util.List;

/**
 * A value which represents the map of potential execution platforms and resolved toolchains. This
 * value only considers a single toolchain type, which allows for a Skyframe cache per toolchain
 * type. Callers will need to consider all toolchain types that are required and merge the results
 * together appropriately.
 */
@AutoCodec
@AutoValue
public abstract class ToolchainResolutionValue implements SkyValue {

  // A key representing the input data.
  public static Key key(
      BuildConfigurationValue.Key configurationKey,
      Label toolchainType,
      ConfiguredTargetKey targetPlatformKey,
      List<ConfiguredTargetKey> availableExecutionPlatformKeys) {
    return Key.create(
        configurationKey, toolchainType, targetPlatformKey, availableExecutionPlatformKeys);
  }

  /** {@link SkyKey} implementation used for {@link ToolchainResolutionFunction}. */
  @AutoCodec
  @AutoCodec.VisibleForSerialization
  @AutoValue
  public abstract static class Key implements SkyKey {

    @Override
    public SkyFunctionName functionName() {
      return SkyFunctions.TOOLCHAIN_RESOLUTION;
    }

    abstract BuildConfigurationValue.Key configurationKey();

    public abstract Label toolchainType();

    abstract ConfiguredTargetKey targetPlatformKey();

    abstract ImmutableList<ConfiguredTargetKey> availableExecutionPlatformKeys();

    @AutoCodec.Instantiator
    static Key create(
        BuildConfigurationValue.Key configurationKey,
        Label toolchainType,
        ConfiguredTargetKey targetPlatformKey,
        List<ConfiguredTargetKey> availableExecutionPlatformKeys) {
      return new AutoValue_ToolchainResolutionValue_Key(
          configurationKey,
          toolchainType,
          targetPlatformKey,
          ImmutableList.copyOf(availableExecutionPlatformKeys));
    }
  }

  @AutoCodec.Instantiator
  public static ToolchainResolutionValue create(
      ImmutableMap<ConfiguredTargetKey, Label> availableToolchainLabels) {
    return new AutoValue_ToolchainResolutionValue(availableToolchainLabels);
  }

  /**
   * Returns the resolved set of toolchain labels (as {@link Label}) for the requested toolchain
   * type, keyed by the execution platforms (as {@link ConfiguredTargetKey}). Ordering is not
   * preserved, if the caller cares about the order of platforms it must take care of that directly.
   */
  public abstract ImmutableMap<ConfiguredTargetKey, Label> availableToolchainLabels();
}
