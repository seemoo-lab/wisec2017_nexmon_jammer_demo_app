![NexMon logo](https://github.com/seemoo-lab/nexmon/raw/master/gfx/nexmon.png)

# WiSec 2017 Nexmon Jammer App

On the 10th ACM Conference on Security and Privacy in Wireless and Mobile Networks (WiSec 2017) 
we published a demo on "Demonstrating Reactive Smartphone-Based Jamming" based on the jammer 
developed for our publication "Massive Reactive Smartphone-Based Jamming using Arbitrary Waveforms
and Adaptive Power Control". This repository contains source code required to rebuild the app shown
during our demonstartion. Additionally, it allows fellow researches to base their own
research on our results.

# Extract from our License

Any use of the Software which results in an academic publication or
other publication which includes a bibliography must include
citations to the nexmon project (1) and the paper cited under (2):

1. "Matthias Schulz, Daniel Wegemer and Matthias Hollick. Nexmon:
    The C-based Firmware Patching Framework. https://nexmon.org"

2. "Matthias Schulz, Francesco Gringoli, Daniel Steinmetzer, Michael
    Koch and Matthias Hollick. Massive Reactive Smartphone-Based
    Jamming using Arbitrary Waveforms and Adaptive Power Control.
    Proceedings of the 10th ACM Conference on Security and Privacy
    in Wireless and Mobile Networks (WiSec 2017), July 2017."

# Getting Started

This repository contains the source code of our app and binaries of the jamming firmware. You can
rebuild the app using Android studio and run it on a rooted Nexus 5 with stock firmware version 
6.0.1 (M4B30Z, Dec 2016). Read our paper to learn how to interact with the app. To fully reproduce
our experiments you need between three and four Nexus 5 smartphones. To avoid easy abuse of our app
and the jamming firmware, we will not publish the APK file. Additionally, we limited the jamming 
firmware to only jam Wi-Fi frames which contain the MAC addresses "NEXMON" and "JAMMER".

# References

* Matthias Schulz, Efstathios Deligeorgopoulos, Matthias Hollick and Francesco Gringoli. **DEMO: Demonstrating Reactive Smartphone-Based Jamming**. Proceedings of the *10th ACM Conference on Security and Privacy in Wireless and Mobile Networks (WiSec 2017)*, July 2017.
* Matthias Schulz, Francesco Gringoli, Daniel Steinmetzer, Michael Koch and Matthias Hollick. **Massive Reactive Smartphone-Based Jamming using Arbitrary Waveforms and Adaptive Power Control**. Proceedings of the *10th ACM Conference on Security and Privacy in Wireless and Mobile Networks (WiSec 2017)*, July 2017.
* Matthias Schulz, Daniel Wegemer and Matthias Hollick. **Nexmon: The C-based Firmware Patching Framework**. https://nexmon.org

[Get references as bibtex file](https://nexmon.org/bib)

# Contact

* [Matthias Schulz](https://seemoo.tu-darmstadt.de/mschulz) <mschulz@seemoo.tu-darmstadt.de>
* [Francesco Gringoli](http://netweb.ing.unibs.it/~gringoli/) <francesco.gringoli@unibs.it>

# Powered By

## Secure Mobile Networking Lab (SEEMOO)
<a href="https://www.seemoo.tu-darmstadt.de">![SEEMOO logo](https://github.com/seemoo-lab/nexmon/raw/master/gfx/seemoo.png)</a>
## Networked Infrastructureless Cooperation for Emergency Response (NICER)
<a href="https://www.nicer.tu-darmstadt.de">![NICER logo](https://github.com/seemoo-lab/nexmon/raw/master/gfx/nicer.png)</a>
## Multi-Mechanisms Adaptation for the Future Internet (MAKI)
<a href="http://www.maki.tu-darmstadt.de/">![MAKI logo](https://github.com/seemoo-lab/nexmon/raw/master/gfx/maki.png)</a>
## University of Brescia
<a href="http://netweb.ing.unibs.it/">![MAKI logo](https://github.com/seemoo-lab/nexmon/raw/master/gfx/brescia.png)</a>
