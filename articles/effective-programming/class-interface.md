# 效率编程 之「类和接口」

### 第 1 条：使类和成员的可访问性最小化

要区别设计良好的模块与设计不好的模块，最重要的因素在于，这个模块对于外部的其他模块而言，是否隐藏其内部数据和其他实现细节。对于顶层的（非嵌套的）类和接口，只有两种可能的访问级别：包级私有（`package-private`）的和公有（`public`）的。如果一个包级私有的的顶层类（或者接口）只是在一个类的内部被用到，就应该考虑使它成为唯一使用它的那个类的私有嵌套类。对于成员（域、方法、嵌套类和嵌套接口）有四种可能的访问级别，下面按照可访问性的递增顺序罗列出来：

- **私有的**（`private`）：只有在声明该成员的顶层类内部才可以访问这个成员；
- **包级私有的**（`package-private`）：声明该成员的包内部的任何类都可以访问这个成员。从技术上讲，它被称为“缺省访问级别”，如果没有为成员指定访问修饰符，就采用这个访问级别；
- **受保护的**（`protected`）：声明该成员的类的子类可以访问这个成员（但有一些限制），并且声明该成员的包内部的任何类也可以访问这个成员；
- **公有的**（`public`）：任何地方都可以访问该成员。

对于公有类的成员，当访问级别从包级私有变成受保护级别时，会大大增加可访问性。受保护的成员是类的导出 API 的一部分，必须永远得到支持。受保护的成员应该尽量少用。**如果方法覆盖了超类中的一个方法，子类中的访问级别就不允许低于超类中的访问级别**。实例域决不能是公有的，否则我们就放弃了强制这个域不可变的能力。并且，包含公有可变域的类并不是线程安全的。

假设常量构成了类提供的整个抽象中的一部分，可以通过公有的静态`final`域来暴露这些常量。注意，长度非零的数组总是可变的。所以，**类具有公有的静态`final`数组域，或者返回这种域的访问方法，这几乎总是错误的**。如果类具有这样的域或者访问方法，客户端将能够修改数组中的内容。这是安全漏洞的一个常见来源：

```
public static final String[] UNSECURITY_DEFAULT_VALUES = {"5", "2", "0"};
```

要注意，许多 IDE 会产生返回指向私有数组域的引用的访问方法，这样就会产生这个问题。修正这个问题有两种方法。**第一种方法**，可以使公有数组变成私有的，并增加一个公有的不可变列表：

```
private static final String[] SECURITY_DEFAULT_VALUES = {"5", "2", "0"};

public static final List<String> SECURITY_DEFAULT_VALUES_LIST =
            Collections.unmodifiableList(Arrays.asList(SECURITY_DEFAULT_VALUES));
```
**第二种方法**，可以使数组变成私有的，并添加一个公有方法，它返回私有数组的一个备份：

```
private static final String[] SECURITY_DEFAULT_VALUES = {"5", "2", "0"};

public static final String[] values() {
    return SECURITY_DEFAULT_VALUES.clone();
}
```
要在这两种方法之间做出选择，得考虑客户端可能怎么处理这个结果。总而言之，我们应该始终尽可能地降低可访问性。除了公有静态`final`域的特殊情形之外，公有类都不应该包含公有域，并且要确保公有静态`final`域所引用的对象都是不可变的。

### 第 2 条：在公有类中使用访问方法而非公有域以及使可变性最小化

说到公有类的时候，毫无疑问，坚持面向对象程序设计思想的看法是正确的：**如果类可以在它所在的包的外部进行访问，就提供访问（`getter`）方法，以保留将来改变该类的内部表示法的灵活性**。然后，如果类是包级私有的，或者是私有的嵌套类，直接暴露它的数据域并没有本质的错误——假设这些数据确实描述了该类所提供的抽象。总之，公有类永远都不应该暴露可变的域。

不可变类只是其实例不能被修改的类。每个实例中包含的所有信息都必须在创建该实例的时候就提供，并在整个对象的整个生命周期内固定不变。存在不可变类有许多理由：不可变类比可变类更加易于设计、实现和使用，它们不容易出错，且更加灵活。为了使类成为不可变的，要遵循下面五条规则：

- 不要提供任何会修改对象状态的方法；
- 保证类不会被扩展，一般做法是使这个类成为`fianl`的；
- 使所有的域都是`fianl`的；
- 使所有的域都成为私有的；
- 确保对于任何可变组件的互斥访问。

不可变对象比较简单，它可以只有一种状态，即被创建时的状态。不可变对象本质上是线程安全的，它们不要求同步。所以，不可变对象可以被自由地共享。不仅可以共享不可变对象，甚至也可以共享它们的内部信息。不可变类真正唯一的缺点是，对于每个不同的值都需要一个单独的对象。

总之，坚决不要为每个`get`方法编写一个相应的`set`方法。除非有很好的理由要让类成为可变的类，否则就应该是不可变的。对于有些类而言，其不可变性是不切实际的。如果类不能被做成是不可变的，仍然应该尽可能地限制它的可变性。因此，除非有令人信服的理由要使域变成是非`final`的，否则要使每个域都是`final`的。

### 第 3 条：接口优于抽象类且只用于定义类型

Java 程序设计语言提供了两种机制，可以用来定义允许多个实现的类型：接口和抽象类。这两种机制之间最明显的区别在于，抽象类允许包含某些方法的实现，但是接口则不允许。一个更为重要的区别在于，为了实现由抽象类定义的类型，类必须成为抽象类的一个子类。因为由于 Java 只允许单继承，所以，抽象类作为类型定义受到了极大的限制。而使用接口，则不然：

- 现有的类可以很容易被更新，以实现新的接口；
- 接口是定义混合类型（`mixin`）的理想选择；
- 接口允许我们构造非层次接口的类型框架。

简而言之，接口通常是定义允许多个实现的类型的最佳途径。当类实现接口时，接口就充当可以引用这个类的实例的类型。因此，类实现了接口，就表明客户端可以对这个类的实例实施某些动作。有一种接口被称为常量接口，它不满足上面的条件，没有任何方法，只包含静态的`final`域，每个域都导出一个常量。使用这些常量的类实现这个接口，以避免用类名来修饰常量名，示例如：

```
public interface PhysicalConstants {
    // Avogadro's number (1/mol)
    static final double AVOGADROS_NUMBER = 6.02214199e23;

    // Boltzmann constant (J/K)
    static final double BOLTZMANN_CONSTANT = 1.3806503e-23;

    // Mass of the electron (kg)
    static final double ELECTRON_MASS = 9.10938199e-31;
}
```

常量接口模式是对接口的不良使用。类在内部使用某些常量，这纯粹是实现细节。实现常量接口，会导致把这样的实现细节泄露到该类的导出 API 中。类实现常量接口，这对于这个类的用户来讲并没有什么价值。实际上，这样做反而会使他们更加糊涂。如果这些常量与某个现有的类或者接口紧密相关，就应该把这些常量添加到这个类或接口中。简而言之，**接口应该只被用来定义类型，它们不应该被用来导出常量**。

### 第 4 条：优先考虑静态成员类

嵌套类是指被定义在另一个类的内部的类。嵌套类存在的目的应该是为它的外围类提供服务。如果嵌套类将来可能会用于其他的环境中，它就应该是顶层类。嵌套类有四种，分别为：静态成员类、非静态成员类、匿名类和局部类。除了第一种之外，其他三种都被称为内部类。

静态成员类是最简单的一个嵌套类。最好把它看做是普通的类，只是碰巧被声明在另一个类的内部而已，它可以访问外围类的所有成员，包括那些声明为私有的成员。静态成员类是外围类的一个静态成员，与其他的静态成员一样，也遵守同样的可访问性规则。静态成员类的一种常见用法是作为公有的辅助类，仅当与它的外部类一起使用时才有意义。

非静态成员类的每个实例都隐含着与外围类的一个外围实例相关联。在非静态成员类的每个实例方法内部，可以调用外围实例上的方法，或者利用修饰过的`this`构造获得外围实例的引用。如果嵌套类的实例可以在它外围类的实例之外独立存在，这个嵌套类就必须是静态成员类：**在没有外围实例的情况下，要想创建非静态成员类的实例是不可能的**。

如果声明成员类不要求访问外围实例，就要始终把`static`修饰符放在它的声明中，使它成为静态成员类，而不是非静态成员类。私有静态成员类的一种常见用法是用来代表外围类所代表的对象的组件，例如`Map`类中的`Entry`对象，对应于`Map`中的每个键值对。匿名类不同于 Java 程序设计语言中的其他任何语法单元，它是在使用的同时被声明和实例化。当且仅当匿名类出现在非静态的环境中时，它才有外围实例。但是即使它们出现在静态的环境中，也不可能拥有任何静态成员。局部类是四种嵌套类中用得最少的类，和匿名内部类一样，也不能包含静态成员。


----------

———— ☆☆☆ —— 返回 -> [那些年，关于 Java 的那些事儿](https://github.com/guobinhit/java-skills/blob/master/README.md) <- 目录 —— ☆☆☆ ————
