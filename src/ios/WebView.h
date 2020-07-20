
#import <UIKit/UIKit.h>
#import <WebKit/WebKit.h>
#import <Cordova/CDV.h>

@interface WebView : CDVPlugin<WKNavigationDelegate> {
  // Member variables go here.
}


- (void)coolMethod:(CDVInvokedUrlCommand*)command;
@end