Tracar (Track Car, the car tracker)
======

Tom Kidman - tom.kidman@gmail.com - 0422 784 326

Built with Java 8 and Maven 3.2.2

To run, first build using 'mvn package' from the root of the project.

Then execute using the 'run.sh' script, also found in the project's root.

This will produce a number of csv files from the sample data that can be found in the 'output' directory.

Design
======

There were 2 decisions I spent some time on.  One was whether to use the LocalDateTime and LocalTime classes in
the domain.  LocalDateTime never seemed like a good fit, as we never had a start date, only a start day.  In the end
I decided that just storing a millisecond value and an int representation for day was easy enough to work with, and
the LocalDateTime class didn't add much.

When initially creating the groups I had a concept of parent child relationships between them (so an hour group would
be made up of 2 half hour groups for example).  This quickly became hard to manage, so I removed the complexity.

A goal I set myself in this challenge was to create simple and easy to understand code.  That's why there's no Spring wiring, no
unnecessary patterns, and I haven't over-engineered for performance or memory use as the sample data didn't require it.
If it's required, I'm able to use all those techniques, but hopefully my drive for simplicity is appreciated here!
