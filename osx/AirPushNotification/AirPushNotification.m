//
//  AirPushNotification.m
//  AirPushNotification
//
//  Created by Peter Nicolai on 6/3/13.
//  Copyright (c) 2013 Fresh Planet. All rights reserved.
//

#import <Adobe AIR/Adobe AIR.h>
#import <Adobe AIR/FlashRuntimeExtensions.h>
#import <AppKit/AppKit.h>
#import <AppKit/NSAlert.h>
#import "AirPushNotification.h"


#define DEFINE_ANE_FUNCTION(fn) FREObject (fn)(FREContext context, void* functionData, uint32_t argc, FREObject argv[])

@implementation AirPushNotification

@end

// set the badge number (count around the app icon)
DEFINE_ANE_FUNCTION(setBadgeNb)
{
    int32_t value;
    if (FREGetObjectAsInt32(argv[0], &value) != FRE_OK)
    {
        return nil;
    }
    
    if (value == 0) {
        [[[NSApplication sharedApplication] dockTile] setBadgeLabel:@""];
    } else {
        [[[NSApplication sharedApplication] dockTile] setBadgeLabel:[NSString stringWithFormat:@"%ld", (long)value]];
    }
    
    return nil;
}





void contextInitializer(void* extData, const uint8_t* ctxType, FREContext ctx, uint32_t* numFunctions, const FRENamedFunction** functions)
{
    *numFunctions = 1;
    FRENamedFunction* func = (FRENamedFunction*) malloc(sizeof(FRENamedFunction) * (*numFunctions));
    
    func[0].name = (const uint8_t*) "setBadgeNb";
    func[0].functionData = NULL;
    func[0].function = &setBadgeNb;
    
    
    *functions = func;
}

void contextFinalizer(FREContext ctx)
{
    return;
}

void initializer(void** extData, FREContextInitializer* ctxInitializer, FREContextFinalizer* ctxFinalizer)
{
    *ctxInitializer = &contextInitializer;
    *ctxFinalizer = &contextFinalizer;
}

void finalizer(void* extData)
{
    return;
}