// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "stringee_plugin",
    platforms: [
        .iOS("13.0")
    ],
    products: [
        .library(name: "stringee-plugin", targets: ["stringee_plugin"])
    ],
    dependencies: [
        .package(name: "FlutterFramework", path: "../FlutterFramework"),
        .package(
            url: "https://github.com/stringeecom/Stringee-iOS-SDK-SPM.git",
            exact: "2.0.3"
        )
    ],
    targets: [
        .target(
            name: "stringee_plugin",
            dependencies: [
                .product(name: "FlutterFramework", package: "FlutterFramework"),
                .product(name: "Stringee", package: "Stringee-iOS-SDK-SPM")
            ],
            cSettings: [
                .headerSearchPath("include/stringee_plugin")
            ]
        )
    ]
)
