import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import { Province } from "./Province.js";
import { sampleProvinceData } from "./Main.js";

describe('province', () => {
  it('shortfall', () => {
    const asia = new Province(sampleProvinceData());
    assert.equal(asia.shortfall, 5);
  });
});
