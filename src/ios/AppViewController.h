#import <UIKit/UIKit.h>
#import <WebKit/WebKit.h>

@interface AppViewController : UIViewController<WKNavigationDelegate>
+ (instancetype)sharedHelper;
- (void)showWebView;
@property (strong,nonatomic) NSString * url;
@property (strong,nonatomic) NSDictionary * headers;
@property WKWebView *webView;
@property UIViewController *webViewController;

@end
