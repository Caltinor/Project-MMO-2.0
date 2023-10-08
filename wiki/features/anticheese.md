[Home](../home.md)

# Anti-Cheese
Is an experience abuse control system.  Anti-Cheese allows you to control certain exploitable elements of XP gains to reduce the xp gained when using exploits.  This system is intended to be open and flexible to allow you as the configurator flexible control.

### Anti-Cheese has 3 types of trackers:
1. AFK Tracking
2. Diminuation Tracking
3. Normalization Tracking

All tracker types are Event-based.  this means that you control how certain events are dealt with.  This also means you can have different configuration for each event type and each event type will have its own tracker to manage that player's restrictions.

## AFK Tracking
When players are not moving, they will begin to accrue AFK time.  when that time reaches the configured threshold, they are considered AFK and will be penalized accordingly.  The longer they remain AFK, the greater the penalty.
```toml
[AntiCheese]
  #if set to true, being AFK can take away XP.  Otherwise, the player eventually earns nothing.
  AFK_Can_Subtract = false
  [AntiCheese.AFK]
    [AntiCheese.AFK.EVENT_TYPE]
      #if an event passes an xp source, you can use this to filter the
      #configuration to only this item/block/entity
      source = ["some:thing"]
      #How long must a player be AFK before this starts to reduce xp
      min_time_to_apply = 200
      #what percent of xp is reduced per half-second spent afk
      reduction = 0.1
      #when the player is not afk, how much of their accrued time 
      #is reduced each half-second they are not afk.
      cooloff_amount = 10
    [AntiCheese.AFK.OTHER_EVENT_TYPE]
      min_time_to_apply = 20 #one second
      reduction = 1.0 #immediate complete loss of xp
      cooloff_amount = 1 #2 seconds of afk time reduced every second
```

## Diminuation Tracking
Is when a player earns the same type of xp in rapid succession.  Common examples include riding, and submersion.  When configured, diminishing xp causes xp to become less and less, over time.  Additionally, the reduction will persist for a set duration so the user has to wait before gaining that xp at full strength again.  This can be used in a similar fashion to AFK tracking but does not require the user to be still.  Both can be used together if needed.
```toml
[AntiCheese]
  [AntiCheese.DiminishingXP]
    [AntiCheese.DiminishingXP.EVENT_TYPE]
      #if an event passes an xp source, you can use this to filter the
      #configuration to only this item/block/entity
      source = ["some:thing"]
      #How long without triggering the same event before the 
      #player earns full xp again.  1 = 0.5 seconds
      retention_duration = 200
      #what percent of xp is reduced per half-second each event proc
      reduction = 0.1
    [AntiCheese.DiminishingXP.OTHER_EVENT_TYPE]
      retention_duration = 200 
      reduction = 0.5 #half xp on second proc and no xp on the next
```

## Normalization Tracking
Is the prevention of extreme values.  Examples include sprinting and swimming where rapid acceleration can cause huge spikes in xp.  Normalization creates tolerance thresholds for how quickly xp can grow and keeps xp earned within those ranges.  Much like diminuation, there is a retention time for how long a normalized value is retained before being reset.  Normalization uses both a flat and percentage tolerance to handle extreme values.  for example a 10% increase tolerance on 10 is 1, but 20 on 200.  A flat tolerance of 15 would permit the 10 on 100, but limit the 200 xp to 15.  In this sense, flat tolerance is the max increase in a single proc, whereas percent scales with the value.
```toml
[AntiCheese]
  [AntiCheese.Normalization]
    [AntiCheese.Normalization.EVENT_TYPE]
      #if an event passes an xp source, you can use this to filter the
      #configuration to only this item/block/entity
      source = ["some:thing"]
      #How long without triggering the same event before the 
      #player earns full xp again.  1 = 0.5 seconds
      retention_duration = 200
      #the maximum flat increase in xp allowed
      tolerance_flat = 10
      #the maximum percent increase in xp allowed
      tolerance_percent = 0.1
    [AntiCheese.Normalization.OTHER_EVENT_TYPE]
      retention_duration = 200 
      tolerance_flat = 15
      tolerance_percent = 0.1
```

[Home](../home.md)