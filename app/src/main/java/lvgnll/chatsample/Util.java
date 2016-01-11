/*
 * Copyright 2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package lvgnll.chatsample;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;

public class Util {
    private static LambdaInvokerFactory sLamFactory;
    private static CognitoCachingCredentialsProvider sCredProvider;

    private static CognitoCachingCredentialsProvider getCredProvider(Context context) {
        if (sCredProvider == null) {
            sCredProvider = new CognitoCachingCredentialsProvider(
                    context.getApplicationContext(),
                    Constants.COGNITO_POOL_ID,
                    Regions.US_EAST_1);
        }
        return sCredProvider;
    }

    public static LambdaInvokerFactory getLamFactory(Context context) {
        if (sLamFactory == null) {
            sLamFactory = new LambdaInvokerFactory(context.getApplicationContext(),
                    Regions.US_EAST_1, getCredProvider(context.getApplicationContext()));
        }
        return sLamFactory;
    }
}
